import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoteContextService } from '../../services/lote-context';
import { LoteHistoricoService, LoteHistoricoItem } from '../../../../shared/service/lote-historico';

@Component({
  selector: 'app-historico',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './historico.html',
  styleUrl: './historico.scss',
})
export class Historico implements OnInit {
  private loteContext = inject(LoteContextService);
  private historicoService = inject(LoteHistoricoService);

  eventos = signal<LoteHistoricoItem[]>([]);
  carregando = signal(false);
  erro = signal<string | null>(null);

  ngOnInit(): void {
    this.carregarHistorico();
  }

  carregarHistorico(): void {
    const loteTriagemId = this.loteContext.loteTriagemId;
    if (!loteTriagemId) return;

    this.erro.set(null);
    this.carregando.set(true);

    this.historicoService.listarPorLote(loteTriagemId).subscribe({
      next: (eventos) => {
        this.carregando.set(false);
        this.eventos.set(eventos);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Erro ao carregar histórico do lote.');
      }
    })
  }

  iconePorTipo(tipo: string): string {
    switch (tipo) {
      case 'LOTE_CRIADO':
        return 'ti ti-file-plus';
      case 'CONFERENCIA_REGISTRADA':
        return 'ti ti-clipboard-check';
      case 'CONTRAPROVA_REGISTRADA':
        return 'ti ti-scale';
      case 'APROVACAO_REGISTRADA':
        return 'ti ti-check';
      default:
        return 'ti ti-circle';
    }
  }

  rotuloPorTipo(tipo: string): string{
    switch (tipo) {
      case 'LOTE_CRIADO':
        return 'Lote criado';
      case 'CONFERENCIA_REGISTRADA':
        return 'Conferência registrada';
      case 'CONTRAPROVA_REGISTRADA':
        return 'Contraprova registrada';
      case 'APROVACAO_REGISTRADA':
        return 'Decisão aprovada';
      default:
        return tipo;
    }
  }
}
