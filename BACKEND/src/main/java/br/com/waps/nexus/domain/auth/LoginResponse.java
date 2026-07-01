package br.com.waps.nexus.domain.auth;

public class LoginResponse {

    private String login;
    private String token;
    private String nome;

    public LoginResponse(String login, String token, String nome) {
        this.login = login;
        this.token = token;
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
