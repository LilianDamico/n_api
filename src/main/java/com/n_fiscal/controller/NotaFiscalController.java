package com.n_fiscal.controller;

import com.n_fiscal.service.NotaFiscalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

@RestController
@RequestMapping("/notas")
@CrossOrigin(origins = "*")
public class NotaFiscalController {

    private final NotaFiscalService notaFiscalService;

    public NotaFiscalController(NotaFiscalService notaFiscalService) {
        this.notaFiscalService = notaFiscalService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarNota(@PathVariable String id) {
        Map<String, AttributeValue> item = notaFiscalService.buscarNotaPorId(id);
        if (item != null) {
            Map<String, Object> resposta = notaFiscalService.converterNota(item);
            return ResponseEntity.ok(resposta);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> inserirNota(@RequestBody Map<String, Object> payload) {
        try {
            notaFiscalService.inserirNota(payload);
            return ResponseEntity.ok("Nota fiscal inserida com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao inserir nota fiscal: " + e.getMessage());
        }
    }
}
