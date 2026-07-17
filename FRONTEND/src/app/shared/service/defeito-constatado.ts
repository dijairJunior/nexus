import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DefeitoConstatado } from '../models/lote-recebido';

@Injectable({ providedIn: 'root' })
export class DefeitoConstatadoService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:3480/api/defeitos';

  listarTodos(): Observable<DefeitoConstatado[]> {
    return this.http.get<DefeitoConstatado[]>(this.baseUrl);
  }
}
