export type StatusAprovacao = 'APROVADO' | 'REPROVADO';

export interface LoteAprovacao {
  id: number;
  loteTriagemId: number;
  gestor: string;
  statusAprovacao: StatusAprovacao;
  motivo: string  | null;
  dataAprovacao: string;
}

export interface NovaAprovacaoPayLoad {
  loteTriagemId: number;
  gestor: string;
  statusAprovacao: StatusAprovacao;
  motivo?: string  | null;
}
