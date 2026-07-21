import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { LoteAprovacao, NovaAprovacaoPayLoad } from '../models/lote-aprovacao';

@Injectable({ providedIn: 'root' })
export class LoteAprovacaoService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/lote-aprovacao`; // sem barra no final

  listarPorLote(loteTriagemId: number): Observable<LoteAprovacao[]> {
    return this.http.get<LoteAprovacao[]>(`${this.baseUrl}/lote/${loteTriagemId}`);
  }

  registrarDecisao(payload: NovaAprovacaoPayLoad): Observable<LoteAprovacao> {
    return this.http.post<LoteAprovacao>(this.baseUrl, payload);
  }
}
