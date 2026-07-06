package br.com.waps.nexus.domain.auth;

import br.com.waps.nexus.domain.usuario.Usuario;
import br.com.waps.nexus.domain.usuario.UsuarioRepository;
import br.com.waps.nexus.exception.ResourceNotFoundException;
import br.com.waps.nexus.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthService authService, JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.autenticar(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String novoToken = jwtService.renovarToken(token);

        Usuario usuario = usuarioRepository.findByLogin(jwtService.extrairLogin(token))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return ResponseEntity.ok(new LoginResponse(usuario.getLogin(), novoToken, usuario.getNome()));
    }
}