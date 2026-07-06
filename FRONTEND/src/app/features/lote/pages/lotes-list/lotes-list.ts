import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoteService } from '../../services/lote';
import { LoteResumo, StatusLote } from '../../../../shared/models/lote';

@Component({
  selector: 'app-lotes-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './lotes-list.html',
  styleUrl: './lotes-list.scss',
})
export class LotesList implements OnInit {
  private loteService = inject(LoteService);

  lotes = signal<LoteResumo[]>([]);
  loading = signal(true);
  erro = signal<string | null>(null);
  pagina = signal(0);
  totalPaginas = signal(0);
  tamanhoPagina = 10;

  progresso = computed(() =>
    this.lotes().map((l) => ({
      ...l,
      percentual:
        l.qtdItens > 0 ? Math.min(100, Math.round((l.qtdConferidos / l.qtdItens) * 100)) : 0,
    })),
  );

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.erro.set(null);
    this.loteService.listarPaginado(this.pagina(), this.tamanhoPagina).subscribe({
      next: (res) => {
        this.lotes.set(res.content);
        this.totalPaginas.set(res.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.erro.set('Erro ao carregar lotes. Tente novamente.');
        this.loading.set(false);
      },
    });
  }

  proximaPagina(): void {
    if (this.pagina() < this.totalPaginas() - 1) {
      this.pagina.update((p) => p + 1);
      this.carregar();
    }
  }

  paginaAnterior(): void {
    if (this.pagina() > 0) {
      this.pagina.update((p) => p - 1);
      this.carregar();
    }
  }
}
