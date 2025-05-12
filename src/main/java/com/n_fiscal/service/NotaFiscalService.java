package com.n_fiscal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotaFiscalService {

    private final DynamoDbClient dynamoDbClient;

    @Value("${dynamodb.table.name}")
    private String tableName;

    public NotaFiscalService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public Map<String, AttributeValue> buscarNotaPorId(String notaFiscalId) {
        Map<String, AttributeValue> chavePrimaria = new HashMap<>();
        chavePrimaria.put("NotaFiscalId", AttributeValue.builder().s(notaFiscalId).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(chavePrimaria)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);
        return response.hasItem() ? response.item() : null;
    }

    public void inserirNota(Map<String, Object> payload) {
        Map<String, AttributeValue> item = new HashMap<>();

        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                item.put(key, AttributeValue.builder().s((String) value).build());
            } else if (value instanceof Number) {
                item.put(key, AttributeValue.builder().n(value.toString()).build());
            } else if (value instanceof List<?>) {
                List<?> lista = (List<?>) value;
                List<AttributeValue> listaDeItens = lista.stream()
                        .map(obj -> {
                            if (obj instanceof Map<?, ?>) {
                                Map<String, AttributeValue> subMap = new HashMap<>();
                                ((Map<?, ?>) obj).forEach((k, v) -> {
                                    if (v instanceof String) {
                                        subMap.put(k.toString(), AttributeValue.builder().s(v.toString()).build());
                                    } else if (v instanceof Number) {
                                        subMap.put(k.toString(), AttributeValue.builder().n(v.toString()).build());
                                    }
                                });
                                return AttributeValue.builder().m(subMap).build();
                            }
                            return AttributeValue.builder().s(obj.toString()).build();
                        })
                        .collect(Collectors.toList());
                item.put(key, AttributeValue.builder().l(listaDeItens).build());
            }
        }

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }

    public Map<String, Object> converterNota(Map<String, AttributeValue> item) {
        Map<String, Object> resposta = new HashMap<>();
        item.forEach((chave, valor) -> {
            if (valor.s() != null) {
                resposta.put(chave, valor.s());
            } else if (valor.n() != null) {
                resposta.put(chave, Double.parseDouble(valor.n()));
            } else if (valor.hasL()) {
                resposta.put(chave, valor.l().stream()
                        .map(AttributeValue::m)
                        .map(this::converterNota)
                        .collect(Collectors.toList()));
            }
        });
        return resposta;
    }
}
