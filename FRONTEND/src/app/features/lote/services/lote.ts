import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaginaLotes, Lote } from '../../../shared/models/lote';

@Injectable({ providedIn: 'root' })
export class LoteService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:3480/api/lotes-triagem'; // corrigido agora !

  listarPaginado(
    page: number,
    size: number,
    sort?: string,
    status?: string,
    protocolo?: string,
    dataInicio?: string,
    dataFim?: string,
  ): Observable<PaginaLotes> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (sort) params = params.set('sort', sort);
    if (status) params = params.set('status', status);
    if (protocolo) params = params.set('protocolo', protocolo);
    if (dataInicio) params = params.set('dataInicio', dataInicio);
    if (dataFim) params = params.set('dataFim', dataFim);
    return this.http.get<PaginaLotes>(`${this.baseUrl}/paginado`, { params });
  }

  buscarPorId(id: number): Observable<Lote> {
    return this.http.get<Lote>(`${this.baseUrl}/${id}`);
  }
}
