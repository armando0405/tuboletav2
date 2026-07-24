package com.tuboleta.backend.service.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.enums.ProviderType;
import com.tuboleta.backend.utils.exception.ScrapingException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Extractor para proveedores de tipo {@code API} (Fase multi-sitio): consume
 * una API JSON en vez de scrapear HTML (más estable). Todo se configura en el
 * {@code config} JSONB del proveedor, sin una clase por sitio.
 *
 * <p>Claves de config: {@code items_path} (ruta con puntos al arreglo de
 * eventos dentro del JSON; vacío = la raíz es el arreglo), {@code id_field},
 * {@code title_field}, {@code venue_field}, {@code city_field},
 * {@code date_field}, {@code user_agent}.</p>
 */
@Component
public class ApiProviderExtractor implements ProviderExtractor {

    private static final Logger log = LogManager.getLogger(ApiProviderExtractor.class);
    private static final String DEFAULT_USER_AGENT = "tuboleta-monitor/1.0";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public boolean supports(Provider provider) {
        return provider != null && provider.getProviderType() == ProviderType.API;
    }

    @Override
    public ExtractionResult extract(Provider provider, String normalizedTerm) throws ScrapingException {
        JsonNode config = parseConfig(provider.getConfig());
        String url = buildSearchUrl(provider.getSearchUrl(), normalizedTerm);
        log.info("API ({}) -> GET {}", provider.getName(), url);
        String body = fetch(url, config);
        ExtractionResult result = parse(body, config);
        log.info("API ({}) -> {} evento(s), {} fallido(s)",
                provider.getName(), result.items().size(), result.failedItems());
        return result;
    }

    String fetch(String url, JsonNode config) throws ScrapingException {
        String userAgent = config.path("user_agent").asText(DEFAULT_USER_AGENT);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", userAgent)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ScrapingException("La API respondió estado " + response.statusCode() + " en " + url, null);
            }
            return response.body();
        } catch (IOException e) {
            throw new ScrapingException("Error al llamar la API: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScrapingException("Llamada a la API interrumpida: " + url, e);
        }
    }

    ExtractionResult parse(String body, JsonNode config) throws ScrapingException {
        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (IOException e) {
            throw new ScrapingException("La API devolvió un JSON inválido", e);
        }
        JsonNode itemsNode = navigate(root, config.path("items_path").asText(""));
        List<RawEventData> out = new ArrayList<>();
        int failed = 0;
        if (itemsNode != null && itemsNode.isArray()) {
            for (JsonNode item : itemsNode) {
                try {
                    out.add(parseItem(item, config));
                } catch (Exception e) {
                    log.warn("No se pudo extraer un evento (API), se omite", e);
                    failed++;
                }
            }
        }
        return new ExtractionResult(out, failed);
    }

    private RawEventData parseItem(JsonNode item, JsonNode config) {
        String id = fieldText(item, config.path("id_field").asText(""));
        String title = fieldText(item, config.path("title_field").asText(""));
        String venue = fieldText(item, config.path("venue_field").asText(""));
        String city = fieldText(item, config.path("city_field").asText(""));
        String date = fieldText(item, config.path("date_field").asText(""));
        String externalId = firstNonBlank(id, title);
        if (externalId == null) {
            throw new IllegalStateException("item de la API sin id ni título");
        }
        return new RawEventData(externalId, title, venue, city, date, item.toString());
    }

    private JsonNode navigate(JsonNode root, String path) {
        if (path == null || path.isBlank()) {
            return root;
        }
        JsonNode current = root;
        for (String part : path.split("\\.")) {
            if (current == null) {
                return null;
            }
            current = current.get(part);
        }
        return current;
    }

    private String fieldText(JsonNode item, String field) {
        if (field == null || field.isBlank()) {
            return null;
        }
        JsonNode node = item.get(field);
        return (node == null || node.isNull()) ? null : blankToNull(node.asText());
    }

    private JsonNode parseConfig(String config) {
        if (config == null || config.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(config);
        } catch (IOException e) {
            log.warn("Config JSONB inválido para el extractor API, se usan defaults", e);
            return objectMapper.createObjectNode();
        }
    }

    private String buildSearchUrl(String template, String normalizedTerm) {
        String encoded = URLEncoder.encode(normalizedTerm == null ? "" : normalizedTerm, StandardCharsets.UTF_8);
        return template.replace("{term}", encoded);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return (b != null && !b.isBlank()) ? b : null;
    }

    private static String blankToNull(String text) {
        return (text == null || text.isBlank()) ? null : text;
    }
}
