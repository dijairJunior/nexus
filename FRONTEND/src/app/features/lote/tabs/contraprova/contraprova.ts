import { Component, effect, inject, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ContraprovaService } from '../../../../shared/service/contraprova';
import { ContraprovaResumo } from '../../../../shared/models/contraprova';
import { LoteContextService } from '../../services/lote-context';

@Component({
  selector: 'app-contraprova',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './contraprova.html',
  styleUrl: './contraprova.scss',
})
export class Contraprova {
  private contraprovaService = inject(ContraprovaService);
  private loteContext = inject(LoteContextService);

  resumo = signal<ContraprovaResumo | null>(null);
  loading = signal(false);
  erro = signal<string | null>(null);

  constructor() {
    effect(() => {
      const loteTriagemId = this.loteContext.loteTriagemId;
      if (loteTriagemId) {
        this.carregar(loteTriagemId);
      }
    });
  }

  carregar(loteTriagemId: number) {
    this.loading.set(true);
    this.erro.set(null);
    this.contraprovaService.gerarContraprova(loteTriagemId).subscribe({
      next: (res) => {
        this.resumo.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.erro.set('Erro ao carregar ao contraprova desse lote');
      },
    });
  }

  badgeClass(status: string): string {
    switch (status) {
      case 'OK':
        return 'badge-ok';
      case 'RECEBIDO_NAO_PREVISTO':
        return 'badge-recebido';
      case 'PREVISTO_NAO_RECEBIDO':
        return 'badge-previsto';
      case 'DIVERGENCIA_CLASSIFICACAO':
        return 'badge-divergencia';
      default:
        return '';
    }
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'OK':
        return 'OK';
      case 'RECEBIDO_NAO_PREVISTO':
        return 'Não Esperado';
      case 'PREVISTO_NAO_RECEBIDO':
        return 'Pendente';
      case 'DIVERGENCIA_CLASSIFICACAO':
        return 'Divergência';
      default:
        return status;
    }
  }
}
