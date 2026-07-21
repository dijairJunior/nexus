import { Component, inject, Inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoteContextService } from '../../services/lote-context';
import { RelatorioLoteService, OpcoesRelatorio } from '../../../../shared/service/relatorio-lote';

@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './relatorios.html',
  styleUrl: './relatorios.scss',
})
export class Relatorios {
  private loteContext = inject(LoteContextService);
  private relatorioService = inject(RelatorioLoteService);

  baixandoCompleto = signal(false);
  baixandoParcial = signal(false);
  erro = signal<string | null>(null);

  incluirProduto = signal(true);
  incluirLoteRecebido = signal(true);
  incluirContraprova = signal(true);
  incluirAprovacao = signal(true);

  nenhumaSecaoSelecionada(): boolean {
    return !this.incluirProduto() && !this.incluirLoteRecebido()
    && !this.incluirContraprova() && !this.incluirAprovacao();
  }

  baixarCompleto(): void {
    const loteTriagemId = this.loteContext.loteTriagemId;
    if (!loteTriagemId) return;

    this.erro.set(null);
    this.baixandoCompleto.set(true);

    this.relatorioService.baixarExcel(loteTriagemId).subscribe({
      next: (arquivo) => {
        this.baixandoCompleto.set(false);
        this.salvarArquivo(arquivo.blob, arquivo.nomeArquivo);
      },
      error: () => {
        this.baixandoCompleto.set(false);
        this.erro.set('Erro ao gerar relatório completo.');
      }
    })
  }

  baixarParcial(): void {
    const loteTriagemId = this.loteContext.loteTriagemId;
    if (!loteTriagemId) return;

    if (this.nenhumaSecaoSelecionada()) {
      this.erro.set('Selecione ao menos uma seção para gerar relatório parcial.');
      return;
    }

    const opcoes: OpcoesRelatorio = {
      produto: this.incluirProduto(),
      loteRecebido: this.incluirLoteRecebido(),
      contraprova: this.incluirContraprova(), // corrigido
      aprovacao: this.incluirAprovacao(),
    }

    this.erro.set(null); // faltava esse também, senão erro anterior nunca some
    this.baixandoParcial.set(true); // faltava esse também! senão o loading nunca liga

    this.relatorioService.baixarExcel(loteTriagemId, opcoes).subscribe({
      next: (arquivo) => {
        this.baixandoParcial.set(false);
        this.salvarArquivo(arquivo.blob, arquivo.nomeArquivo); // corrigido
      },
      error: () => {
        this.baixandoParcial.set(false);
        this.erro.set('Erro ao gerar relatório parcial.');
      }
    })
  }

  private salvarArquivo(blob: Blob, nomeArquivo: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nomeArquivo;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
