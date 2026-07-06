export interface Lote {
  id: number;
  numero: number;
  descricao: string;
  dataLote: string;

  protocolo: string;
  cnpjCliente: string;
  razaoSocialCliente: string;
  enderecoCliente: string;
  contatoCliente: string;
  quantidadeEsperada: number;
}

export interface LoteResumo {
  id: number;
  dataLote: string; // LocalDate → ISO string (yyyy-MM-dd)
  status: StatusLote;
  qtdItens: number;
  qtdConferidos: number;
}

export type StatusLote =
  | 'LOTE_A_RECEBER'
  | 'RECEBIDO'
  | 'EM_TRIAGEM'
  | 'EM_CONTRAPROVA'
  | 'AGUARDANDO_APROVACAO'
  | 'APROVADO'
  | 'REPROVADO'
  | 'FINALIZADO';

export interface PaginaLotes {
  content: LoteResumo[];
  tatelElements: number;
  totalPages: number;
  number: number // página atual
  siza:number;
}
