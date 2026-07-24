package com.tuboleta.backend.service.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.enums.ProviderType;
import com.tuboleta.backend.utils.exception.ScrapingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extractor SCRAPER para TuBoleta.com (REQ-FUE-001).
 *
 * <p>Separa la obtención de la página ({@link #fetch}) del parseo del DOM
 * ({@link #parse}) para poder probar el parseo contra fixtures de HTML
 * real sin red.</p>
 *
 * <p>No filtra por término de búsqueda ni toca la base de datos: extrae
 * todo lo que la página devuelve para el término ya incluido en la URL de
 * búsqueda. El filtrado (REQ-BUS-003) es responsabilidad de la capa de
 * detección de cambios.</p>
 *
 * <p>Estructura del HTML esperada (ver fixtures en
 * {@code src/test/resources/fixtures/}): un {@code article.bg-grey-light}
 * por show; los campos se extraen SOLO dentro de su
 * {@code a.content-link-container}, ya que cada article contiene además un
 * {@code div.modal} interno con datos de fecha distintos que NO deben
 * contaminar la extracción.</p>
 */
@Component
public class TuBoletaScraperExtractor implements ProviderExtractor {

    private static final Logger log = LogManager.getLogger(TuBoletaScraperExtractor.class);

    private static final String PROVIDER_NAME = "TuBoleta";
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int DEFAULT_TIMEOUT_MS = 15_000;

    /**
     * Instancia propia y no gestionada por Spring: en Spring Boot 4 el
     * {@code ObjectMapper} auto-configurado es {@code tools.jackson}
     * (Jackson 3), no {@code com.fasterxml.jackson} — se crea localmente
     * para no acoplar este extractor a esa configuración.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(Provider provider) {
        return provider != null
                && provider.getProviderType() == ProviderType.SCRAPER
                && PROVIDER_NAME.equals(provider.getName());
    }

    @Override
    public ExtractionResult extract(Provider provider, String normalizedTerm) throws ScrapingException {
        String url = buildSearchUrl(provider.getSearchUrl(), normalizedTerm);
        log.info("Scraping TuBoleta -> GET {}", url);
        Document doc = fetch(url, provider.getConfig());
        ExtractionResult result = parse(doc);
        log.info("Scraping TuBoleta -> {} evento(s) en la página, {} no se pudo(pudieron) parsear",
                result.items().size(), result.failedItems());
        return result;
    }

    /**
     * Descarga y parsea el HTML de {@code url}, usando {@code user_agent} y
     * {@code timeout_ms} tomados del {@code config} JSONB del proveedor (con
     * defaults si faltan o el JSON es inválido).
     *
     * @throws ScrapingException si falla la conexión/descarga
     */
    Document fetch(String url, String config) throws ScrapingException {
        JsonNode configNode = parseConfig(config);
        String userAgent = configNode.path("user_agent").asText(DEFAULT_USER_AGENT);
        int timeoutMs = configNode.path("timeout_ms").asInt(DEFAULT_TIMEOUT_MS);
        try {
            return Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeoutMs)
                    .get();
        } catch (IOException e) {
            throw new ScrapingException("Error al obtener la página de TuBoleta: " + url, e);
        }
    }

    /**
     * Parsea el listado de resultados de búsqueda ya descargado. No lanza
     * excepción por ítems individuales: cada {@code article} que falla al
     * extraerse se cuenta en {@code failedItems} y se continúa con los
     * demás (corrida parcial, REQ-DET-005).
     */
    ExtractionResult parse(Document doc) {
        Elements articles = doc.select("article.bg-grey-light");
        List<RawEventData> items = new ArrayList<>();
        int failedItems = 0;
        for (Element article : articles) {
            try {
                items.add(parseArticle(article));
            } catch (Exception e) {
                log.warn("No se pudo extraer un evento del HTML de TuBoleta, se omite y se continúa", e);
                failedItems++;
            }
        }
        return new ExtractionResult(items, failedItems);
    }

    private JsonNode parseConfig(String config) {
        if (config == null || config.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(config);
        } catch (IOException e) {
            log.warn("Config JSONB inválido para el proveedor TuBoleta, se usan valores por defecto", e);
            return objectMapper.createObjectNode();
        }
    }

    private String buildSearchUrl(String searchUrlTemplate, String normalizedTerm) {
        String encodedTerm = URLEncoder.encode(
                normalizedTerm == null ? "" : normalizedTerm, StandardCharsets.UTF_8);
        return searchUrlTemplate.replace("{term}", encodedTerm);
    }

    private RawEventData parseArticle(Element article) {
        Element link = article.selectFirst("a.content-link-container");
        if (link == null) {
            throw new IllegalStateException("article sin a.content-link-container");
        }
        String href = link.attr("href");
        if (href.isBlank()) {
            throw new IllegalStateException("a.content-link-container sin href");
        }

        Element contentInfo = link.selectFirst(".content-info");
        String title = extractTitle(contentInfo);
        String venue = extractGreyText(contentInfo, 0);
        String city = extractGreyText(contentInfo, 1);
        String eventDateRaw = extractEventDateRaw(link);

        return new RawEventData(href, title, venue, city, eventDateRaw,
                buildRawJson(href, title, venue, city, eventDateRaw));
    }

    private String extractTitle(Element contentInfo) {
        if (contentInfo == null) {
            return null;
        }
        Element span = contentInfo.selectFirst("div.fw-bold.text-uppercase span");
        return blankToNull(span == null ? null : span.text());
    }

    private String extractGreyText(Element contentInfo, int index) {
        if (contentInfo == null) {
            return null;
        }
        Elements greyDivs = contentInfo.select("div.text-grey");
        if (greyDivs.size() <= index) {
            return null;
        }
        Element span = greyDivs.get(index).selectFirst("span");
        return blankToNull(span == null ? null : span.text());
    }

    private String extractEventDateRaw(Element link) {
        Element datesContainer = link.selectFirst(".dates-container");
        if (datesContainer == null) {
            return null;
        }
        Elements blocks = datesContainer.select(".content-date");
        List<String> parts = new ArrayList<>();
        for (Element block : blocks) {
            String part = extractDateBlockText(block);
            if (part != null) {
                parts.add(part);
            }
        }
        return parts.isEmpty() ? null : String.join(" - ", parts);
    }

    private String extractDateBlockText(Element block) {
        Element mobile = block.selectFirst("span.fs-7.fw-bold");
        if (mobile != null && !mobile.text().isBlank()) {
            return mobile.text();
        }
        Element day = block.selectFirst("span.fs-5.fw-bold");
        Element month = block.selectFirst("span.fs-8.fw-bold");
        if (day == null || month == null) {
            return null;
        }
        String dayText = day.text();
        String monthText = month.text();
        if (dayText.isBlank() || monthText.isBlank()) {
            return null;
        }
        return dayText + " " + monthText;
    }

    private String buildRawJson(String href, String title, String venue, String city, String eventDateRaw) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("href", href);
        fields.put("title", title);
        fields.put("venue", venue);
        fields.put("city", city);
        fields.put("eventDateRaw", eventDateRaw);
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar rawJson", e);
        }
    }

    private static String blankToNull(String text) {
        return (text == null || text.isBlank()) ? null : text;
    }
}
