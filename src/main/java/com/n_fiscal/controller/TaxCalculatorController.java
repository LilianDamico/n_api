package com.n_fiscal.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.n_fiscal.dto.TaxCalculationRequest;
import com.n_fiscal.service.TaxCalculatorService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin(origins = "*")
public class TaxCalculatorController {

    private final TaxCalculatorService taxCalculatorService;

    @Autowired
    public TaxCalculatorController(TaxCalculatorService taxCalculatorService) {
        this.taxCalculatorService = taxCalculatorService;
    }

    @PostMapping("/calcular-tributos")
    public Map<String, Double> calcularTributo(@RequestBody TaxCalculationRequest request) {
        double totalTributos = taxCalculatorService.calcularTributo(
            request.getUfOrigem(),
            request.getUfDestino(),
            request.getValorOperacao()
        );

        Map<String, Double> resposta = new HashMap<>();
        resposta.put("totalTributos", totalTributos);
        return resposta;
    }
}
