import { Injectable, inject} from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';

export type TipoEventoHistorico =
  | 'LOTE_CRIADO'
  | 'CONFERENCIA_REGISTRADA'
  | 'CONTRAPROVA_REGISTRADA'
  | 'APROVACAO_REGISTRADA';

export interface LoteHistoricoItem {
  id: number;
  tipoEvento: TipoEventoHistorico;
  descricao: string;
  usuario: string | null;
  dataEvento: string;
}

@Injectable({ providedIn: 'root' })
export class LoteHistoricoService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:3480/api/lote-historico';

  listarPorLote(loteTriagemId: number): Observable<LoteHistoricoItem[]> {
    return this.http.get<LoteHistoricoItem[]>(`${this.baseUrl}/${loteTriagemId}`);
  }
}
