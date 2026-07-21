import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface OpcoesRelatorio {
  produto?: boolean;
  loteRecebido?: boolean;
  contraprova?: boolean;
  aprovacao?: boolean;
}

export interface ArquivoRelatorio {
  blob: Blob;
  nomeArquivo: string;
}

@Injectable({ providedIn: 'root' })
export class RelatorioLoteService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:3480/api/relatorio/lote';

  baixarExcel(loteTriagemId: number, opcoes?: OpcoesRelatorio): Observable<ArquivoRelatorio> {
    let params = new HttpParams();

    if (opcoes) {
      if (opcoes.produto !== undefined) params = params.set('produto', opcoes.produto);
      if (opcoes.loteRecebido !== undefined)
        params = params.set('loteRecebido', opcoes.loteRecebido);
      if (opcoes.contraprova !== undefined) params = params.set('contraprova', opcoes.contraprova);
      if (opcoes.aprovacao !== undefined) params = params.set('aprovacao', opcoes.aprovacao);
    }

    return this.http
      .get(`${this.baseUrl}/${loteTriagemId}/excel`, {
        params,
        responseType: 'blob',
        observe: 'response',
      })
      .pipe(
        map((response: HttpResponse<Blob>) => ({
          blob: response.body as Blob,
          nomeArquivo: this.extrairNomeArquivo(response.headers.get('Content-Disposition')),
        })),
      );
  }

  private extrairNomeArquivo(contentDisposition: string | null): string {
    if (!contentDisposition) return 'Relatorio_Lote.xlsx';
    const match = contentDisposition.match(/filename="(.+)"/);
    return match ? match[1] : 'Relatorio_Lote.xlsx';
  }
}
