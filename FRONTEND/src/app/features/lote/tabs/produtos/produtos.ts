import { Component, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Produto } from '../../../../shared/models/produto';
import { ProdutosService } from '../../../../shared/service/produto';
import { LoteDetalhe } from '../../pages/lote-detalhe/lote-detalhe';

@Component({
  selector: 'app-produtos',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './produtos.html',
  styleUrl: './produtos.scss',
})
export class Produtos {
  // [PRODUTO] inínio
  private produtoService = inject(ProdutosService);
  private loteDatelhe = inject(LoteDetalhe);

  produtos = signal<Produto[]>([]);
  loading = signal(true);
  erro = signal<string | null>(null);

  private carregouUmaVez = false;

  constructor() {
    effect(() => {
      const lote = this.loteDatelhe.lote();
      if (lote && !this.carregouUmaVez) {
        this.carregouUmaVez = true;
        this.carregar(lote.id);
      }
    });
  }

  carregar(loteTriagemId: number): void {
    this.loading.set(true);
    this.erro.set(null);
    this.produtoService.listarPorLote(loteTriagemId).subscribe({
      next: (produtos) => {
        this.produtos.set(produtos);
        this.loading.set(false);
      },
      error: () => {
        this.erro.set('Erro ao carregar produto do lote');
        this.loading.set(false);
      }
    });
  }
}
