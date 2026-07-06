import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaginaLotes } from '../../../shared/models/lote';

@Injectable({ providedIn: 'root' })
export class LoteService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:3480/api/lotes-triagem'; // corrigido agora !

  listarPaginado(
    page: number,
    size: number,
    sort?: string,
    status?: string,
  ): Observable<PaginaLotes> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (sort) params = params.set('sort', sort);
    if (status) params = params.set('status', status);
    return this.http.get<PaginaLotes>(`${this.baseUrl}/paginado`, { params });
  }
}
