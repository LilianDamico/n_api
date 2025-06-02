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

    @PostMapping
    public ResponseEntity<String> salvarNota(@RequestBody NotaFiscalDTO dto) {
        notaFiscalService.salvarNota(dto);
        return ResponseEntity.ok("Nota salva com sucesso.");
    }

    @GetMapping("/listar")
    public ResponseEntity<List<NotaFiscalDTO>> listarTodasNotas() {
        return ResponseEntity.ok(notaFiscalService.listarTodasNotas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaFiscalDTO> buscarNotaPorId(@PathVariable String id) {
        NotaFiscalDTO nota = notaFiscalService.buscarNotaDTO(id);
        if (nota != null) {
            return ResponseEntity.ok(nota);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarNota(@PathVariable String id, @RequestBody NotaFiscalDTO dto) {
        boolean atualizada = notaFiscalService.atualizarNota(id, dto);
        if (atualizada) {
            return ResponseEntity.ok("Nota atualizada com sucesso.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarNota(@PathVariable String id) {
        boolean deletada = notaFiscalService.deletarNotaPorId(id);
        if (deletada) {
            return ResponseEntity.ok("Nota deletada com sucesso.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> consultarEstatisticas() {
        return ResponseEntity.ok(notaFiscalService.consultarEstatisticas());
    }

    @GetMapping("/vendas-produto")
    public ResponseEntity<Integer> consultarVendasPorProdutoEmMes(
            @RequestParam String codProduto,
            @RequestParam String mesAno // formato: YYYY-MM
    ) {
        int vendas = notaFiscalService.consultarVendasProdutoPorMes(codProduto, mesAno);
        return ResponseEntity.ok(vendas);
    }

    @GetMapping("/faturamento")
    public ResponseEntity<Double> consultarFaturamentoPorMes(@RequestParam String mesAno) {
        double total = notaFiscalService.consultarFaturamentoPorMes(mesAno);
        return ResponseEntity.ok(total);
    }
}
