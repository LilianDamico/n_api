package com.n_fiscal.dto;

import java.util.List;

public class NotaFiscalDTO {

    private String notaFiscalId;
    private String nomeCliente;
    private String cpfCnpj;
    private String enderecoEntrega;
    private String dataCompra;

    private List<ItemNotaDTO> itens;
    private double totalNota;
    private double totalTributos;

    // Getters e Setters

    public String getNotaFiscalId() {
        return notaFiscalId;
    }

    public void setNotaFiscalId(String notaFiscalId) {
        this.notaFiscalId = notaFiscalId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public String getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(String dataCompra) {
        this.dataCompra = dataCompra;
    }

    public List<ItemNotaDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemNotaDTO> itens) {
        this.itens = itens;
    }

    public double getTotalNota() {
        return totalNota;
    }

    public void setTotalNota(double totalNota) {
        this.totalNota = totalNota;
    }

    public double getTotalTributos() {
        return totalTributos;
    }

    public void setTotalTributos(double totalTributos) {
        this.totalTributos = totalTributos;
    }
}
