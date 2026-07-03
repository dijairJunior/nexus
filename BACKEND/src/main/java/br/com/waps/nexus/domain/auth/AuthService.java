package br.com.waps.nexus.domain.auth;

import br.com.waps.nexus.domain.usuario.Usuario;
import br.com.waps.nexus.domain.usuario.UsuarioRepository;
import br.com.waps.nexus.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    public LoginResponse autenticar(LoginRequest request) {
        System.out.println("AUTH 1 - entrou");

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getSenha());

        System.out.println("AUTH 2 - antes authenticate");

        var authentication = authenticationManager.authenticate(authToken);

        System.out.println("AUTH 3 - depois authenticate");

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        System.out.println("AUTH 4 - antes buscar usuario");

        Usuario usuario = usuarioRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        System.out.println("AUTH 5 - usuario encontrado");

        String token = jwtService.gerarToker(userDetails.getUsername());

        System.out.println("AUTH 6 - token gerado");

        System.out.println("AUTH 7 - antes return");

        return new LoginResponse(usuario.getLogin(), token, usuario.getNome());
    }
}
