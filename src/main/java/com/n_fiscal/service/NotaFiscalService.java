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

    public boolean atualizarNota(String id, NotaFiscalDTO dto) {
        NotaFiscalDTO existente = buscarNotaDTO(id);
        if (existente == null) return false;
        dto.setNotaFiscalId(id);
        salvarNota(dto);
        return true;
    }

    public boolean deletarNotaPorId(String id) {
        Map<String, AttributeValue> key = Map.of("NotaFiscalId", AttributeValue.builder().s(id).build());
        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();
        dynamoDbClient.deleteItem(request);
        return true;
    }

    public NotaFiscalDTO buscarNotaDTO(String id) {
        Map<String, AttributeValue> item = buscarNotaPorId(id);
        if (item == null || item.isEmpty()) return null;

        NotaFiscalDTO dto = new NotaFiscalDTO();
        dto.setNotaFiscalId(item.get("NotaFiscalId").s());
        dto.setNomeCliente(item.get("NomeCliente").s());
        dto.setCpfCnpj(item.get("CPF_CNPJ").s());
        dto.setEnderecoEntrega(item.get("EnderecoEntrega").s());
        dto.setDataCompra(item.get("DataCompra").s());

        try {
            List<ItemNotaDTO> itens = objectMapper.readValue(item.get("Itens").s(), new TypeReference<>() {});
            dto.setItens(itens);
            double total = itens.stream().mapToDouble(i -> i.getQuantidade() * i.getPrecoUnitario()).sum();
            double tributos = total * 0.18;
            dto.setTotalNota(total);
            dto.setTotalTributos(tributos);
        } catch (JsonProcessingException e) {
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
            dto.setNotaFiscalId(item.get("NotaFiscalId").s());
            dto.setNomeCliente(item.get("NomeCliente").s());
            dto.setCpfCnpj(item.get("CPF_CNPJ").s());
            dto.setEnderecoEntrega(item.get("EnderecoEntrega").s());
            dto.setDataCompra(item.get("DataCompra").s());

            try {
                List<ItemNotaDTO> itens = objectMapper.readValue(item.get("Itens").s(), new TypeReference<>() {});
                dto.setItens(itens);
                double total = itens.stream().mapToDouble(i -> i.getQuantidade() * i.getPrecoUnitario()).sum();
                double tributos = total * 0.18;
                dto.setTotalNota(total);
                dto.setTotalTributos(tributos);
            } catch (JsonProcessingException e) {
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

    public int consultarVendasProdutoPorMes(String codProduto, String mesAno) {
        List<NotaFiscalDTO> notas = listarTodasNotas();
        return notas.stream()
                .filter(n -> n.getDataCompra().startsWith(mesAno))
                .flatMap(n -> n.getItens().stream())
                .filter(i -> i.getCodProduto().equalsIgnoreCase(codProduto))
                .mapToInt(ItemNotaDTO::getQuantidade)
                .sum();
    }

    public double consultarFaturamentoPorMes(String mesAno) {
        return listarTodasNotas().stream()
                .filter(n -> n.getDataCompra().startsWith(mesAno))
                .mapToDouble(NotaFiscalDTO::getTotalNota)
                .sum();
    }

    private Map<String, AttributeValue> buscarNotaPorId(String id) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("NotaFiscalId", AttributeValue.builder().s(id).build()))
                .build();

        return dynamoDbClient.getItem(request).item();
    }

    private Map<String, AttributeValue> montarItem(NotaFiscalDTO dto) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("NotaFiscalId", AttributeValue.fromS(dto.getNotaFiscalId()));
        item.put("NomeCliente", AttributeValue.fromS(dto.getNomeCliente()));
        item.put("CPF_CNPJ", AttributeValue.fromS(dto.getCpfCnpj()));
        item.put("EnderecoEntrega", AttributeValue.fromS(dto.getEnderecoEntrega()));
        item.put("DataCompra", AttributeValue.fromS(dto.getDataCompra()));

        double total = dto.getItens().stream()
                .mapToDouble(i -> i.getQuantidade() * i.getPrecoUnitario())
                .sum();
        double tributo = total * 0.18;

        item.put("TotalNota", AttributeValue.fromS(String.format(Locale.US, "%.2f", total)));
        item.put("TotalTributos", AttributeValue.fromS(String.format(Locale.US, "%.2f", tributo)));

        try {
            item.put("Itens", AttributeValue.fromS(objectMapper.writeValueAsString(dto.getItens())));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar itens", e);
        }

        return item;
    }
}
