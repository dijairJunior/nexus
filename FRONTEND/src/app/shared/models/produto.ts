export interface Produto {
  id: number;

  numero: number;
  preenchimentoObrig: string;
  codigoSap: string;
  descricao: string;
  quantidade: number;
  modelo: string;

  numeroPatrimonio: string;
  numeroSerie: string;
  sgp: string;
  numeroImobilizado: string;
  subNumeroImobilizado: string;

  condicaoBem: string;
  alienarOuArmazenar: string;
  statusProcesso: string;
  statusVenda: string;
  status2: string;
  statusCadastro: string;

  custoAquisicao: string;
  classificacao: string;
  saldoContabil: number;

  uf: string;
  nfVenda: string;
  reversaClaro: string;

  dataCompra: string;
  dataSeparacao: string;
  dataEntradaNajason: string;

  loteTriagemId: number;

  cor: string;
  capacidade: string;
  estetica: string;
  realizadoRecovery: boolean;
  triador: string;
  validaImei: boolean;
  tipoTriagem: string;

  nfDevolucao: string;
  dataNfDevolucao: string;
  centroDistribuicao: string;

  criadoEm: string;
  atualizadoEm: string;

  statusItem: string;
  defeitoConstatadoId: number;

  resetado: string;
}
