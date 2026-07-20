export interface LoteRecebido {
  id: number;
  numeroSerie: string;
  estetica: string;
  defeitoConstatadoId: number;
  classificacao: string;
  statusItem: string;
  resetado: string;
  capacidadeArmazenamento: number;
  possuiMarcasUso: boolean;
  loteTriagemId: number;
  triador: string;
  dataConferencia: string;
  statusConferencia: string;
  observacao: string;
}

export interface LoteRecebidoRequest {
  numeroSerie: string;
  estetica: string;
  defeitoConstatadoId: number | null;
  statusItem: string;
  resetado: string;
  capacidadeArmazenamento: number | null;
  possuiMarcasUso: boolean | null;
  loteTriagemId: number;
  triador: string;
  statusConferencia?: string;
  observacao?: string;
}

export interface DefeitoConstatado {
  id: number;
  descricao: string;
}
