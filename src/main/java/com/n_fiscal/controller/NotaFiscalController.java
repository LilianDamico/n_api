package com.n_fiscal.controller;

import com.n_fiscal.dto.NotaFiscalDTO;
import com.n_fiscal.service.NotaFiscalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notas")
public class NotaFiscalController {

    private final NotaFiscalService notaFiscalService;

    public NotaFiscalController(NotaFiscalService notaFiscalService) {
        this.notaFiscalService = notaFiscalService;
    }

    // ✅ 1. Listar todas as notas
    @GetMapping("/listar")
    public ResponseEntity<List<NotaFiscalDTO>> listarTodasNotas() {
        List<NotaFiscalDTO> notas = notaFiscalService.listarTodasNotas();
        return ResponseEntity.ok(notas);
    }

    // ✅ 2. Buscar nota por ID
    @GetMapping("/{id}")
    public ResponseEntity<NotaFiscalDTO> buscarNotaDTO(@PathVariable String id) {
        NotaFiscalDTO dto = notaFiscalService.buscarNotaDTO(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    // ✅ 3. Salvar nova nota
    @PostMapping
    public ResponseEntity<String> salvarNota(@RequestBody NotaFiscalDTO dto) {
        try {
            notaFiscalService.salvarNota(dto);
            return ResponseEntity.ok("Nota salva com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao salvar nota: " + e.getMessage());
        }
    }

    // ✅ 4. Estatísticas
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> consultarEstatisticas() {
        return ResponseEntity.ok(notaFiscalService.consultarEstatisticas());
    }
}
