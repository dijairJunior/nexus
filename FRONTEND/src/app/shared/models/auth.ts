export interface LoginRequest {
  login: string;
  senha: string;
}

export interface LoginResponse {
  login: string;
  token: string;
  nome: string;
}
