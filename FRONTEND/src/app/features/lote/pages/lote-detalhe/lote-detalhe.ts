import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ActivatedRoute,
  Router,
  RouterOutlet,
  RouterLink,
  RouterLinkActive,
} from '@angular/router';
import { LoteService } from '../../services/lote';
import { Lote } from '../../../../shared/models/lote';

@Component({
  selector: 'app-lote-detalhe',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './lote-detalhe.html',
  styleUrl: './lote-detalhe.scss',
})
export class LoteDetalhe implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private loteService = inject(LoteService);

  // Estado compartilhado — buscado uma única vez, consumido por todas as abas
  lote = signal<Lote | null>(null);
  loading = signal(true);
  erro = signal<string | null>(null);

  // Definição das abas — usado no template pra montar os links de navegação
  abas = [
    { path: 'informacoes', label: 'Informações' },
    { path: 'produtos', label: 'Produtos' },
    { path: 'conferencia', label: 'Conferência' },
    { path: 'contraprova', label: 'Contraprova' },
    { path: 'aprovacao', label: 'Aprovação' },
    { path: 'relatorios', label: 'Relatórios' },
    { path: 'historico', label: 'Histórico' },
  ];

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.erro.set('ID de lote inválido.');
      this.loading.set(false);
      return;
    }
    this.carregar(id);
  }

  carregar(id: number): void {
    this.loading.set(true);
    this.erro.set(null);
    this.loteService.buscarPorId(id).subscribe({
      next: (lote) => {
        this.lote.set(lote);
        this.loading.set(false);
      },
      error: () => {
        this.erro.set('Erro ao carregar o lote. Tente novamente.');
        this.loading.set(false);
      },
    });
  }

  voltar(): void {
    this.router.navigate(['/lotes']);
  }
}
