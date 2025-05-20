package com.n_fiscal.service;

import org.springframework.stereotype.Service;

@Service
public class TaxCalculatorService {

    public double calcularTributo(String ufOrigem, String ufDestino, double valorOperacao) {
        double aliquota;

        if (ufOrigem.equalsIgnoreCase(ufDestino)) {
            // Operação dentro do mesmo estado (ex: SP → SP)
            aliquota = 0.18;
        } else {
            // Operação interestadual (ex: SP → RJ)
            aliquota = 0.12;
        }

        return valorOperacao * aliquota;
    }
}
