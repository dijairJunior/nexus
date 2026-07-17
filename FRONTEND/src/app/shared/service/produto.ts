import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Produto } from '../models/produto';

@Injectable({ providedIn: 'root' })
export class ProdutosService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:3480/api/produtos';

  buscarPorNumeroSerie(numeroSerie: string): Observable<Produto> {
    return this.http.get<Produto>(`${this.baseUrl}/serie/${numeroSerie}`);
  }

  buscarPorSeries(numerosSerie: string[]): Observable<Produto[]> {
    return this.http.post<Produto[]>(`${this.baseUrl}/buscar-por-series`, numerosSerie);
  }

  listarPorLote(loteTriagemId: number): Observable<Produto[]> {
    return this.http.get<Produto[]>(`${this.baseUrl}/lote/${loteTriagemId}`);
  }
}
