package com.n_fiscal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n_fiscal.dto.ItemNotaDTO;
import com.n_fiscal.dto.NotaFiscalDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Service
public class NotaFiscalService {

    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${dynamodb.table.name}")
    private String tableName;

    public NotaFiscalService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void salvarNota(NotaFiscalDTO dto) {
        Map<String, AttributeValue> item = montarItem(dto);

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }

    public NotaFiscalDTO buscarNotaDTO(String id) {
        Map<String, AttributeValue> item = buscarNotaPorId(id);
        if (item == null || item.isEmpty()) return null;

        NotaFiscalDTO dto = new NotaFiscalDTO();

        dto.setNotaFiscalId(getAttr(item, "NotaFiscalId"));
        dto.setNomeCliente(getAttr(item, "NomeCliente"));
        dto.setCpfCnpj(getAttr(item, "CPF_CNPJ"));
        dto.setEnderecoEntrega(getAttr(item, "EnderecoEntrega"));
        dto.setDataCompra(getAttr(item, "DataCompra"));

        String jsonItens = getAttr(item, "Itens");
        try {
            List<ItemNotaDTO> itens = objectMapper.readValue(jsonItens, new TypeReference<>() {});
            dto.setItens(itens);

            double total = calcularTotal(itens);
            double tributos = calcularTributos(total);

            dto.setTotalNota(total);
            dto.setTotalTributos(tributos);
        } catch (Exception e) {
            dto.setItens(List.of());
            dto.setTotalNota(0.0);
            dto.setTotalTributos(0.0);
        }

        return dto;
    }

    public List<NotaFiscalDTO> listarTodasNotas() {
        ScanRequest request = ScanRequest.builder().tableName(tableName).build();
        List<Map<String, AttributeValue>> items = dynamoDbClient.scan(request).items();

        List<NotaFiscalDTO> notas = new ArrayList<>();
        for (Map<String, AttributeValue> item : items) {
            NotaFiscalDTO dto = new NotaFiscalDTO();

            dto.setNotaFiscalId(getAttr(item, "NotaFiscalId"));
            dto.setNomeCliente(getAttr(item, "NomeCliente"));
            dto.setCpfCnpj(getAttr(item, "CPF_CNPJ"));
            dto.setEnderecoEntrega(getAttr(item, "EnderecoEntrega"));
            dto.setDataCompra(getAttr(item, "DataCompra"));

            String jsonItens = getAttr(item, "Itens");
            try {
                List<ItemNotaDTO> itens = objectMapper.readValue(jsonItens, new TypeReference<>() {});
                dto.setItens(itens);

                double total = calcularTotal(itens);
                double tributos = calcularTributos(total);

                dto.setTotalNota(total);
                dto.setTotalTributos(tributos);
            } catch (Exception e) {
                dto.setItens(List.of());
                dto.setTotalNota(0.0);
                dto.setTotalTributos(0.0);
            }

            notas.add(dto);
        }

        return notas;
    }

    public Map<String, Object> consultarEstatisticas() {
        List<NotaFiscalDTO> notas = listarTodasNotas();

        Map<String, Long> notasPorMes = new HashMap<>();
        double totalTributos = 0;
        double faturamentoTotal = 0;

        for (NotaFiscalDTO nota : notas) {
            String mes = nota.getDataCompra().substring(0, 7); // YYYY-MM
            notasPorMes.put(mes, notasPorMes.getOrDefault(mes, 0L) + 1);

            faturamentoTotal += nota.getTotalNota();
            totalTributos += nota.getTotalTributos();
        }

        return Map.of(
                "C1_notas_por_mes", notasPorMes,
                "C2_faturamento_total", faturamentoTotal,
                "C3_total_tributos", totalTributos
        );
    }

    public Map<String, AttributeValue> buscarNotaPorId(String id) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("NotaFiscalId", AttributeValue.builder().s(id).build()))
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);
        return response.item();
    }

    public Map<String, AttributeValue> montarItem(NotaFiscalDTO dto) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("NotaFiscalId", AttributeValue.builder().s(dto.getNotaFiscalId()).build());
        item.put("NomeCliente", AttributeValue.builder().s(dto.getNomeCliente()).build());
        item.put("CPF_CNPJ", AttributeValue.builder().s(dto.getCpfCnpj()).build());
        item.put("EnderecoEntrega", AttributeValue.builder().s(dto.getEnderecoEntrega()).build());
        item.put("DataCompra", AttributeValue.builder().s(dto.getDataCompra()).build());

        double total = calcularTotal(dto.getItens());
        double tributos = calcularTributos(total);

        item.put("TotalNota", AttributeValue.builder().s(String.format(Locale.US, "%.2f", total)).build());
        item.put("TotalTributos", AttributeValue.builder().s(String.format(Locale.US, "%.2f", tributos)).build());

        try {
            String json = objectMapper.writeValueAsString(dto.getItens());
            item.put("Itens", AttributeValue.builder().s(json).build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar itens", e);
        }

        return item;
    }

    // ---------- Métodos auxiliares ----------
    private double calcularTotal(List<ItemNotaDTO> itens) {
        return itens.stream()
                .mapToDouble(i -> i.getQuantidade() * i.getPrecoUnitario())
                .sum();
    }

    private double calcularTributos(double total) {
        return total * 0.18;
    }

    private String getAttr(Map<String, AttributeValue> item, String key) {
        return Optional.ofNullable(item.get(key))
                .map(AttributeValue::s)
                .orElse("");
    }
}
