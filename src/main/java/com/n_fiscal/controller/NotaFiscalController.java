package com.n_fiscal.controller;

import com.n_fiscal.dto.NotaFiscalDTO;
import com.n_fiscal.service.NotaFiscalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notas")
@CrossOrigin(origins = "*")
public class NotaFiscalController {

    private final NotaFiscalService notaFiscalService;

    public NotaFiscalController(NotaFiscalService notaFiscalService) {
        this.notaFiscalService = notaFiscalService;
    }

    // POST - Criar nota
    @PostMapping
    public ResponseEntity<Void> criarNota(@RequestBody NotaFiscalDTO dto) {
        notaFiscalService.salvarNota(dto);
        return ResponseEntity.ok().build();
    }

    // GET - Buscar uma nota por ID
    @GetMapping("/{id}")
    public ResponseEntity<NotaFiscalDTO> buscarNota(@PathVariable String id) {
        NotaFiscalDTO dto = notaFiscalService.buscarNotaDTO(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    // GET - Listar todas as notas
    @GetMapping("/listar")
    public ResponseEntity<List<NotaFiscalDTO>> listarTodasNotas() {
        return ResponseEntity.ok(notaFiscalService.listarTodasNotas());
    }

    // GET - Estatísticas
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> estatisticas() {
        return ResponseEntity.ok(notaFiscalService.consultarEstatisticas());
    }

    // PUT - Atualizar uma nota existente
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarNota(@PathVariable String id, @RequestBody NotaFiscalDTO dto) {
        notaFiscalService.atualizarNota(id, dto);
        return ResponseEntity.ok().build();
    }

    // DELETE - Deletar uma nota
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarNota(@PathVariable String id) {
        notaFiscalService.deletarNota(id);
        return ResponseEntity.ok().build();
    }
}
