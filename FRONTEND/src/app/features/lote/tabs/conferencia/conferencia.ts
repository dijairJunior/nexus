import { Component, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoteRecebido, LoteRecebidoRequest, DefeitoConstatado } from '../../../../shared/models/lote-recebido';
import { LoteRecebidoService } from '../../../../shared/service/lote-recebido';
import { DefeitoConstatadoService } from '../../../../shared/service/defeito-constatado';
import { LoteDetalhe } from '../../pages/lote-detalhe/lote-detalhe';
import { ProdutosService } from '../../../../shared/service/produto';

@Component({
  selector: 'app-conferencia',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './conferencia.html',
  styleUrl: './conferencia.scss',
})
export class Conferencia {
  // [CONFERENCIA] início
  private loteRecebido = inject(LoteRecebidoService);
  private defeitoConstatado = inject(DefeitoConstatadoService);
  private loteDetalhe = inject(LoteDetalhe);
  private produtoService = inject(ProdutosService);

  itens = signal<LoteRecebido[]>([]);
  defeitos = signal<DefeitoConstatado[]>([]);
  loading = signal(true);
  erro = signal<string | null>(null);

  mostrarFormulario = signal(false);
  salvando = signal(false);
  erroFormulario = signal<string | null>(null);

  modeloEncontrado = signal<string | null>(null);
  buscandoModelo = signal(false);
  modelosPorSerie = signal<Map<string, string>>(new Map());

  // Valores válidos (regra de negócio fixa do projeto)
  readonly opcoesEstetica = ['BOM', 'RISCOS LEVES', 'RISCOS PROFUNDOS'];
  readonly opcoesResetado = ['SIM', 'NÃO'];
  readonly opcoesStatusItem = ['NOVO', 'OBSOLETO', 'TRIADO'];

  form: LoteRecebidoRequest = this.formVazio();

  private loteId: number | null = null;
  private carregouUmaVez = false;

  constructor() {
    effect(() => {
      const lote = this.loteDetalhe.lote();
      if (lote && !this.carregouUmaVez) {
        this.carregouUmaVez = true;
        this.loteId = lote.id;
        this.form.loteTriagemId = lote.id;
        this.carregar(lote.id);
        this.carregarDefeitos();
      }
    });
  }

  private formVazio(): LoteRecebidoRequest {
    return {
      numeroSerie: '',
      estetica: '',
      defeitoConstatadoId: null,
      statusItem: '',
      resetado: '',
      loteTriagemId: this.loteId ?? 0,
      triador: '',
      observacao: '',
    };
  }

  private buscarModelosDaLista(itens: LoteRecebido[]): void {
    const mapa = new Map<string, string>();
    itens.forEach((item) => {
      this.produtoService.buscarPorNumeroSerie(item.numeroSerie).subscribe({
        next: (produto) => {
          mapa.set(item.numeroSerie, produto.modelo || '—');
          this.modelosPorSerie.set(new Map(mapa));
        },
        error: () => {
          mapa.set(item.numeroSerie, '—');
          this.modelosPorSerie.set(new Map(mapa));
        },
      });
    });
  }

  private buscarModelosEmLote(itens: LoteRecebido[]): void {
    if (itens.length === 0) return;

    const series = itens.map((i) => i.numeroSerie);
    this.produtoService.buscarPorSeries(series).subscribe({
      next: (produtos) => {
        const mapa = new Map<string, string>();
        produtos.forEach((p) => mapa.set(p.numeroSerie, p.modelo || '—'));
        // itens sem correspondência na ARM ficam marcados
        series.forEach((s) => {
          if (!mapa.has(s)) mapa.set(s, 'Não encontrado na ARM');
        });
        this.modelosPorSerie.set(mapa);
      },
      error: () => {
        // não bloqueia a tela — coluna fica com '...' permanente em caso de falha
      },
    });
  }

  onImeiChange(valor: string): void {
    this.form.numeroSerie = valor;
    this.modeloEncontrado.set(null);

    if (valor.length === 15) {
      this.buscandoModelo.set(true);
      this.produtoService.buscarPorNumeroSerie(valor).subscribe({
        next: (produto) => {
          this.modeloEncontrado.set(produto.modelo || 'Modelo não informado na ARM');
          this.buscandoModelo.set(false);
        },
        error: () => {
          this.modeloEncontrado.set('IMEI não encontrado na ARM (produto não previsto)');
          this.buscandoModelo.set(false);
        },
      });
    }
  }

  carregar(loteTriagemId: number): void {
    this.loading.set(true);
    this.erro.set(null);
    this.loteRecebido.listarPorLote(loteTriagemId).subscribe({
      next: (itens) => {
        this.itens.set(itens);
        this.loading.set(false);
        this.buscarModelosEmLote(itens);
      },
      error: () => {
        this.erro.set('Erro ao carregar itens conferidos.');
        this.loading.set(false);
      },
    });
  }

  carregarDefeitos(): void {
    this.defeitoConstatado.listarTodos().subscribe({
      next: (defeitos) => this.defeitos.set(defeitos),
      error: () => {
        // não bloqueia a tela — só o select de defeito fica vazio
      },
    });
  }

  abrirFormulario(): void {
    this.form = this.formVazio();
    this.erroFormulario.set(null);
    this.mostrarFormulario.set(true);
  }

  fecharFormulario(): void {
    this.mostrarFormulario.set(false);
  }

  salvar(): void {
    if (!this.loteId) return;

    this.erroFormulario.set(null);

    if (!this.form.numeroSerie || this.form.numeroSerie.length !== 15) {
      this.erroFormulario.set('IMEI deve ter exatamente 15 dígitos!');
      return;
    }

    this.salvando.set(true);
    this.loteRecebido.registrarConferencia(this.form).subscribe({
      next: () => {
        this.salvando.set(false);
        this.mostrarFormulario.set(false);
        this.carregar(this.loteId!);
      },
      error: (err) => {
        this.salvando.set(false);
        const msg = err?.error?.message || 'Erro ao registrar conferência.';
        this.erroFormulario.set(msg);
      },
    });
  }

  modeloDoItem(numeroSerie: string): string {
    return this.modelosPorSerie().get(numeroSerie) ?? '...';
  }
  // [CONFERENCIA] fim
}
