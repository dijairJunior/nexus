export type StatusContraprova =
  | 'OK'
  | 'RECEBIDO_NAO_PREVISTO'
  | 'PREVISTO_NAO_RECEVIDO'
  | 'DIVERGENCIA_CLASSIFICACAO'

export interface ContraprovaItem {
  numeroSerie: string;
  status: StatusContraprova;
  classificacaoEsperada: string | null;
  classificacaoRecebida: string | null;
}

export interface ContraprovaResumo {
  loteTreiagemId: number;
  itens: ContraprovaItem[];
  totalEsperados: number;
  totalRecebidos: number;
  totalOk: number;
  totalRecebidoNaoPrevisto: number;
  totalPrevistoNaoRecebido: number;
  totalDivergenciaClassificacao: number;
}
