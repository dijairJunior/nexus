import { Injectable, signal } from '@angular/core';
import { Lote } from '../../../shared/models/lote';

@Injectable()
export class LoteContextService {
  lote = signal<Lote | null>(null);

  setLote(lote: Lote | null) {
    this.lote.set(lote);
  }

  get loteTriagemId(): number | null {
    return this.lote()?.id ?? null;
  }
}
