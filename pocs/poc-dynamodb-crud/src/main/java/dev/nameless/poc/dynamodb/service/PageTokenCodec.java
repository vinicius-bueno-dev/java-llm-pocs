package dev.nameless.poc.dynamodb.service;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Codec simples para converter um {@code LastEvaluatedKey} do DynamoDB num token
 * opaco (Base64) e vice-versa. Suporta apenas atributos do tipo String, o que e
 * suficiente para a tabela desta POC onde pk, sk, email e createdAt sao strings.
 *
 * <p>Em producao, use serializacao JSON dedicada ou um formato binario. Aqui a
 * ideia e manter o codigo didatico.</p>
 */
public final class PageTokenCodec {

    private PageTokenCodec() {}

    public static String encode(Map<String, AttributeValue> lastEvaluatedKey) {
        if (lastEvaluatedKey == null || lastEvaluatedKey.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        lastEvaluatedKey.forEach((k, v) -> {
            if (sb.length() > 0) sb.append(";");
            sb.append(k).append("=").append(v.s() == null ? "" : v.s());
        });
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static Map<String, AttributeValue> decode(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        Map<String, AttributeValue> result = new HashMap<>();
        for (String part : raw.split(";")) {
            int idx = part.indexOf('=');
            if (idx < 0) continue;
            result.put(part.substring(0, idx), AttributeValue.fromS(part.substring(idx + 1)));
        }
        return result;
    }
}
