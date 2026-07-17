package br.com.waps.nexus.domain.defeito;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/defeitos")
public class DefeitoConstatadoController {

    private final DefeitoConstatadoRepository defeitoConstatadoRepository;

    public DefeitoConstatadoController(DefeitoConstatadoRepository defeitoConstatadoRepository) {
        this.defeitoConstatadoRepository = defeitoConstatadoRepository;
    }

    @GetMapping
    public ResponseEntity<List<DefeitoConstatado>> listarTodos() {
        return ResponseEntity.ok(defeitoConstatadoRepository.findAll());
    }
}
