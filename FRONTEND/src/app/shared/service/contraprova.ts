import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { ContraprovaResumo } from '../../shared/models/contraprova';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ContraprovaService {
  private readonly baseUrl = `${environment.apiUrl}/contraprova`;

  constructor(private http: HttpClient) {}

  gerarContraprova(loteTriagemId: number): Observable<ContraprovaResumo> {
    return this.http.get<ContraprovaResumo>(`${this.baseUrl}/lote/${loteTriagemId}`)
  }
}
