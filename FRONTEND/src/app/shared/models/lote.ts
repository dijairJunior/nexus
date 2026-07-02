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
