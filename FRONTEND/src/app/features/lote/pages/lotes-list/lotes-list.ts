import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoteService } from '../../services/lote';
import { LoteResumo, StatusLote } from '../../../../shared/models/lote';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-lotes-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './lotes-list.html',
  styleUrl: './lotes-list.scss',
})
export class LotesList implements OnInit {
  private loteService = inject(LoteService);
  private router = inject(Router);

  lotes = signal<LoteResumo[]>([]);
  loading = signal(true);
  erro = signal<string | null>(null);
  pagina = signal(0);
  totalPaginas = signal(0);
  tamanhoPagina = 10;

  filtroStatus = signal<string>('');
  filtroProtocolo = signal<string>('');
  filtroDataInicio = signal<string>('');
  filtroDataFim = signal<string>('');

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
    this.loteService.listarPaginado(
      this.pagina(),
      this.tamanhoPagina,
      undefined,
      this.filtroStatus() || undefined,
      this.filtroProtocolo() || undefined,
      this.filtroDataInicio() || undefined,
      this.filtroDataFim() || undefined,
    )
      .subscribe({
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

  aplicarFiltros(): void {
    this.pagina.set(0);
    this.carregar();
  }

  limparFiltros(): void {
    this.filtroStatus.set('');
    this.filtroProtocolo.set('');
    this.filtroDataInicio.set('');
    this.filtroDataFim.set('');
    this.pagina.set(0);
    this.carregar();
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

  abrirDetalhe(id: number): void {
    this.router.navigate(['/lotes', id]);
  }
}
