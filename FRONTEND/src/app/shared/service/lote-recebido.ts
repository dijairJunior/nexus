import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoteRecebido, LoteRecebidoRequest } from '../models/lote-recebido';

@Injectable({ providedIn: 'root' })
export class LoteRecebidoService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:3480/api/lote-recebido';

  listarPorLote(LoteTriagemId: number): Observable<LoteRecebido[]> {
    return this.http.get<LoteRecebido[]>(`${this.baseUrl}/lote/${LoteTriagemId}`);
  }

  registrarConferencia(dto: LoteRecebidoRequest): Observable<LoteRecebido> {
    return this.http.post<LoteRecebido>(`${this.baseUrl}/registrar-conferencia`, dto);
  }
}
