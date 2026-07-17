import { Component, inject, computed} from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoteDetalhe } from '../../pages/lote-detalhe/lote-detalhe';

@Component({
  selector: 'app-informacoes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './informacoes.html',
  styleUrl: './informacoes.scss',
})
export class Informacoes {
  // [INFORMACOES] início - acessa o lote carregado uma única vez pelo componente pai
  private LoteDetalhe = inject(LoteDetalhe);

  lote = computed(() => this.LoteDetalhe.lote());
}
