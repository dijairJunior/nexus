import { Component, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoteContextService } from '../../services/lote-context';
import { LoteAprovacao, StatusAprovacao } from '../../../../shared/models/lote-aprovacao';
import { LoteAprovacaoService } from '../../../../shared/service/lote-aprovacao'

@Component({
  selector: 'app-aprovacao',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './aprovacao.html',
  styleUrl: './aprovacao.scss',
})
export class Aprovacao {
  private loteContext = inject(LoteContextService);
  private aprovacaoService = inject(LoteAprovacaoService);

  historico = signal<LoteAprovacao[]>([]);
  loading = signal(false);
  erro = signal<string | null>(null);

  gestor = signal('');
  statusAprovacao = signal<StatusAprovacao>('APROVADO');
  motivo = signal('');
  enviando = signal(false);
  erroForm = signal<string | null>(null);

  constructor() {
    effect(() => {
      const loteTriagemId = this.loteContext.loteTriagemId;
      if (loteTriagemId) {
        this.carregar(loteTriagemId);
      }
    });
  }

  carregar(loteTriagemId: number): void {
    this.loading.set(true);
    this.erro.set(null);

    this.aprovacaoService.listarPorLote(loteTriagemId).subscribe({
      next: (historico) => {
        this.historico.set(historico);
        this.loading.set(false);
      },
      error: () => {
        this.erro.set('Erro ao carregar histórico de aprovação.');
        this.loading.set(false);
      }
    });
  }

  registrar(): void {
    const loteTriagemId = this.loteContext.loteTriagemId;
    if (!loteTriagemId) return;

    if (!this.gestor().trim()) {
      this.erroForm.set('Informe o gestor responsável.')
      return;
    }

    if (this.statusAprovacao() === 'REPROVADO' && !this.motivo().trim()) {
      this.erroForm.set('Motivo é obrigatório para reprovação.');
      return;
    }

    this.erroForm.set(null);
    this.enviando.set(true);

    this.aprovacaoService.registrarDecisao({
      loteTriagemId,
      gestor: this.gestor(),
      statusAprovacao: this.statusAprovacao(),
      motivo: this.motivo().trim() || undefined
    }).subscribe({
      next: () => {
        this.enviando.set(false);
        this.motivo.set('');
        this.carregar(loteTriagemId);
      },
      error: (err) => {
        this.enviando.set(false);
        this.erroForm.set(err?.error?.message ?? 'Erro ao carregar decisão.')
      }
    })
  }

  badgeClass(status: StatusAprovacao): string {
    return status === "APROVADO" ? "badge-aprovado" : "badge-reprovado";
  }

  protected readonly status = status;
}
