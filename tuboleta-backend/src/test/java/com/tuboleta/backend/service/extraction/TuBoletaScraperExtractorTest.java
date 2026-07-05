package com.tuboleta.backend.service.extraction;

import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.enums.ProviderType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica el parseo del extractor de TuBoleta contra fixtures de HTML REAL
 * de la página (ver {@code requerimientos/EJEMPLO-HTML-alcolirykoz.md} y
 * {@code requerimientos/ejemplo-html-fuck-news-tuboleta.md}). Los valores
 * esperados se derivaron leyendo esos fixtures, no se inventaron.
 */
class TuBoletaScraperExtractorTest {

    private final TuBoletaScraperExtractor extractor = new TuBoletaScraperExtractor();

    private Document loadFixture(String filename) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("fixtures/" + filename)) {
            assertThat(is).as("fixture %s debe existir en test/resources", filename).isNotNull();
            return Jsoup.parse(is, "UTF-8", "https://www.tuboleta.com/");
        }
    }

    @Test
    void alcolirykozFixture_extractsExactlyThreeItemsWithNoFailures() throws IOException {
        Document doc = loadFixture("tuboleta-alcolirykoz.html");

        ExtractionResult result = extractor.parse(doc);

        assertThat(result.failedItems()).isZero();
        assertThat(result.items()).hasSize(3);

        RawEventData segundaFecha = result.items().get(0);
        assertThat(segundaFecha.externalId()).isEqualTo("/es/eventos/alcolirykoz-dos-decadaz-segunda-fecha");
        assertThat(segundaFecha.title()).isEqualTo("ALCOLIRYKOZ DOS DÉCADAZ - SEGUNDA FECHA");
        assertThat(segundaFecha.venue()).isEqualTo("Mundo Aventura");
        assertThat(segundaFecha.city()).isEqualTo("Bogotá");
        assertThat(segundaFecha.eventDateRaw()).isEqualTo("5 Jul");

        RawEventData cali = result.items().get(1);
        assertThat(cali.externalId()).isEqualTo("/es/eventos/alcolirykoz-dos-decadaz-cali");
        assertThat(cali.title()).isEqualTo("ALCOLIRYKOZ DOS DÉCADAZ - CALI");
        assertThat(cali.venue()).isEqualTo("Centro de eventos La Colina");
        assertThat(cali.city()).isEqualTo("Cali");
        assertThat(cali.eventDateRaw()).isEqualTo("18 Jul");

        RawEventData manizales = result.items().get(2);
        assertThat(manizales.externalId()).isEqualTo("/es/eventos/alcolirykoz-dos-decadaz-manizales-0");
        assertThat(manizales.title()).isEqualTo("ALCOLIRYKOZ DOS DÉCADAZ - MANIZALES");
        assertThat(manizales.venue()).isEqualTo("Carpa Monarca - Manizales, Caldas");
        assertThat(manizales.city()).isEqualTo("Manizales");
        assertThat(manizales.eventDateRaw()).isEqualTo("1 Ago");
    }

    @Test
    void segundaFechaShow_dateComesFromCardNotFromModal() throws IOException {
        Document doc = loadFixture("tuboleta-alcolirykoz.html");

        ExtractionResult result = extractor.parse(doc);

        RawEventData segundaFecha = result.items().stream()
                .filter(item -> item.externalId().equals("/es/eventos/alcolirykoz-dos-decadaz-segunda-fecha"))
                .findFirst()
                .orElseThrow();

        assertThat(segundaFecha.eventDateRaw()).contains("5 Jul");
        assertThat(segundaFecha.eventDateRaw()).doesNotContain("4 Jul");
    }

    @Test
    void fucksNewsFixture_extractsExactlyOneItem() throws IOException {
        Document doc = loadFixture("tuboleta-fucks-news.html");

        ExtractionResult result = extractor.parse(doc);

        assertThat(result.failedItems()).isZero();
        assertThat(result.items()).hasSize(1);

        RawEventData item = result.items().get(0);
        assertThat(item.externalId()).isEqualTo("/es/eventos/fucks-news-noticreo-cartagena-0");
        assertThat(item.title()).isEqualTo("FUCKS NEWS NOTICREO - CARTAGENA");
        assertThat(item.venue()).isNull();
        assertThat(item.city()).isEqualTo("Cartagena");
        assertThat(item.eventDateRaw()).isEqualTo("21 Jul");
    }

    @Test
    void htmlWithoutArticles_returnsEmptyListWithNoFailuresAndNoException() {
        Document doc = Jsoup.parse("<html><body><p>Sin resultados</p></body></html>");

        ExtractionResult result = extractor.parse(doc);

        assertThat(result.items()).isEmpty();
        assertThat(result.failedItems()).isZero();
    }

    @Test
    void mutilatedArticleMixedWithValidOne_oneItemAndOneFailed() {
        String html = """
                <html><body>
                <div class="view-content">
                  <article class="p-3 h-100 bg-grey-light rounded-1 position-relative">
                    <!-- mutilado: sin a.content-link-container -->
                    <div class="clearfix p-0 h-100 fields-container">
                      <span>Evento roto, sin enlace</span>
                    </div>
                  </article>
                  <article class="p-3 h-100 bg-grey-light rounded-1 position-relative">
                    <div class="clearfix p-0 h-100 fields-container">
                      <a class="content-link-container" href="/es/eventos/evento-valido">
                        <div class="content-info">
                          <div class="fs-8 text-uppercase fw-bold"><span>EVENTO VALIDO</span></div>
                          <div class="text-grey"><span>Venue Valido</span></div>
                          <div class="text-grey"><span>Medellín</span></div>
                        </div>
                        <div class="dates-container">
                          <div class="content-date">
                            <span class="fs-7 fw-bold">10 Ago</span>
                          </div>
                        </div>
                      </a>
                    </div>
                  </article>
                </div>
                </body></html>
                """;
        Document doc = Jsoup.parse(html);

        ExtractionResult result = extractor.parse(doc);

        assertThat(result.failedItems()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).externalId()).isEqualTo("/es/eventos/evento-valido");
    }

    @Test
    void supports_onlyTrueForTuBoletaScraperProvider() {
        Provider tuBoletaScraper = Provider.builder()
                .name("TuBoleta")
                .providerType(ProviderType.SCRAPER)
                .build();
        Provider tuBoletaApi = Provider.builder()
                .name("TuBoleta")
                .providerType(ProviderType.API)
                .build();
        Provider otherScraper = Provider.builder()
                .name("OtroProveedor")
                .providerType(ProviderType.SCRAPER)
                .build();

        assertThat(extractor.supports(tuBoletaScraper)).isTrue();
        assertThat(extractor.supports(tuBoletaApi)).isFalse();
        assertThat(extractor.supports(otherScraper)).isFalse();
        assertThat(extractor.supports(null)).isFalse();
    }
}
