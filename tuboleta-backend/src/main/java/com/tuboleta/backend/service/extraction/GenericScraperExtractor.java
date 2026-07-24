package com.tuboleta.backend.service.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.enums.ProviderType;
import com.tuboleta.backend.utils.exception.ScrapingException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

/**
 * Extractor SCRAPER genérico configurable (Fase multi-sitio): permite agregar
 * sitios NUEVOS sin escribir una clase por cada uno. Los selectores CSS viven
 * en el {@code config} JSONB del proveedor, no en el código.
 *
 * <p>Aplica a proveedores {@code SCRAPER} distintos de TuBoleta (que tiene su
 * propio extractor con estructura compleja) cuyo {@code config} trae
 * {@code item_selector}. Claves de config soportadas:
 * {@code item_selector} (obligatoria), {@code title_selector},
 * {@code venue_selector}, {@code city_selector}, {@code date_selector},
 * {@code link_selector}, {@code link_attr} (default "href"),
 * {@code user_agent}, {@code timeout_ms}.</p>
 */
@Component
public class GenericScraperExtractor implements ProviderExtractor {

    private static final Logger log = LogManager.getLogger(GenericScraperExtractor.class);
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int DEFAULT_TIMEOUT_MS = 15_000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(Provider provider) {
        return provider != null
                && provider.getProviderType() == ProviderType.SCRAPER
                && !"TuBoleta".equals(provider.getName())
                && hasItemSelector(provider.getConfig());
    }

    @Override
    public ExtractionResult extract(Provider provider, String normalizedTerm) throws ScrapingException {
        JsonNode config = parseConfig(provider.getConfig());
        String url = buildSearchUrl(provider.getSearchUrl(), normalizedTerm);
        log.info("Scraping genérico ({}) -> GET {}", provider.getName(), url);
        Document doc = fetch(url, config);
        ExtractionResult result = parse(doc, config);
        log.info("Scraping genérico ({}) -> {} evento(s), {} fallido(s)",
                provider.getName(), result.items().size(), result.failedItems());
        return result;
    }

    Document fetch(String url, JsonNode config) throws ScrapingException {
        String userAgent = config.path("user_agent").asText(DEFAULT_USER_AGENT);
        int timeoutMs = config.path("timeout_ms").asInt(DEFAULT_TIMEOUT_MS);
        try {
            return Jsoup.connect(url).userAgent(userAgent).timeout(timeoutMs).get();
        } catch (IOException e) {
            throw new ScrapingException("Error al obtener la página: " + url, e);
        }
    }

    ExtractionResult parse(Document doc, JsonNode config) {
        Elements items = doc.select(config.path("item_selector").asText(""));
        List<RawEventData> out = new ArrayList<>();
        int failed = 0;
        for (Element item : items) {
            try {
                out.add(parseItem(item, config));
            } catch (Exception e) {
                log.warn("No se pudo extraer un evento (scraper genérico), se omite", e);
                failed++;
            }
        }
        return new ExtractionResult(out, failed);
    }

    private RawEventData parseItem(Element item, JsonNode config) {
        String title = text(item, config.path("title_selector").asText(""));
        String venue = text(item, config.path("venue_selector").asText(""));
        String city = text(item, config.path("city_selector").asText(""));
        String date = text(item, config.path("date_selector").asText(""));
        String link = attr(item, config.path("link_selector").asText(""),
                config.path("link_attr").asText("href"));
        String externalId = firstNonBlank(link, title);
        if (externalId == null) {
            throw new IllegalStateException("item sin link ni título para usar como id");
        }
        return new RawEventData(externalId, title, venue, city, date,
                rawJson(externalId, title, venue, city, date));
    }

    private static String text(Element item, String selector) {
        if (selector == null || selector.isBlank()) {
            return null;
        }
        Element el = item.selectFirst(selector);
        return el == null ? null : blankToNull(el.text());
    }

    private static String attr(Element item, String selector, String attribute) {
        if (selector == null || selector.isBlank()) {
            return null;
        }
        Element el = item.selectFirst(selector);
        return el == null ? null : blankToNull(el.attr(attribute));
    }

    private boolean hasItemSelector(String config) {
        JsonNode node = parseConfig(config);
        return !node.path("item_selector").asText("").isBlank();
    }

    private JsonNode parseConfig(String config) {
        if (config == null || config.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(config);
        } catch (IOException e) {
            log.warn("Config JSONB inválido para el scraper genérico, se usan defaults", e);
            return objectMapper.createObjectNode();
        }
    }

    private String buildSearchUrl(String template, String normalizedTerm) {
        String encoded = URLEncoder.encode(normalizedTerm == null ? "" : normalizedTerm, StandardCharsets.UTF_8);
        return template.replace("{term}", encoded);
    }

    private String rawJson(String externalId, String title, String venue, String city, String date) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("externalId", externalId);
        fields.put("title", title);
        fields.put("venue", venue);
        fields.put("city", city);
        fields.put("eventDateRaw", date);
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar rawJson", e);
        }
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
