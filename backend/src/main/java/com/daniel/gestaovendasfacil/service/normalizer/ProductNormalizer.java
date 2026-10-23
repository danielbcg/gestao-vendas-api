package com.daniel.gestaovendasfacil.service.normalizer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class ProductNormalizer {

    private final ObjectMapper objectMapper;
    private final Map<String, String> sinonimos = new HashMap<>();

    public ProductNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void carregarSinonimos() {
        try {
            ClassPathResource resource = new ClassPathResource("produtos-sinonimos.json");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    Map<String, String> map = objectMapper.readValue(is, new TypeReference<>() {});
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        String chaveNormalizada = normalizeBasic(entry.getKey());
                        sinonimos.put(chaveNormalizada, entry.getValue().trim());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível carregar produtos-sinonimos.json: " + e.getMessage());
        }
    }

    public String normalize(String rawProduto) {
        if (rawProduto == null || rawProduto.trim().isEmpty()) {
            return "Produto não especificado";
        }

        String basic = normalizeBasic(rawProduto);

        if (sinonimos.containsKey(basic)) {
            return sinonimos.get(basic);
        }

        return basic;
    }

    public String normalizeBasic(String text) {
        if (text == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return semAcentos.replaceAll("\\s+", " ").trim();
    }
}

