package com.tuboleta.backend.service.extraction;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.enums.ProviderType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

class GenericScraperExtractorTest {

    private final GenericScraperExtractor extractor = new GenericScraperExtractor();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String CONFIG = """
            {"item_selector":".event","title_selector":".t","venue_selector":".v",
             "city_selector":".c","date_selector":".d","link_selector":".lnk","link_attr":"href"}""";

    private static final String HTML = """
            <html><body>
              <div class="event">
                <h3 class="t">Concierto A</h3>
                <span class="v">Arena</span><span class="c">Bogotá</span><span class="d">5 Jul</span>
                <a class="lnk" href="/e/1">ver</a>
              </div>
              <div class="event">
                <h3 class="t">Concierto B</h3>
                <a class="lnk" href="/e/2">ver</a>
              </div>
            </body></html>""";

    @Test
    void parse_extractsItemsBySelectors() throws Exception {
        Document doc = Jsoup.parse(HTML);
        JsonNode config = mapper.readTree(CONFIG);

        ExtractionResult result = extractor.parse(doc, config);

        assertThat(result.items()).hasSize(2);
        assertThat(result.failedItems()).isZero();
        RawEventData first = result.items().get(0);
        assertThat(first.title()).isEqualTo("Concierto A");
        assertThat(first.venue()).isEqualTo("Arena");
        assertThat(first.city()).isEqualTo("Bogotá");
        assertThat(first.eventDateRaw()).isEqualTo("5 Jul");
        assertThat(first.externalId()).isEqualTo("/e/1");
        // El segundo no tiene venue/city/date -> null, id = link.
        RawEventData second = result.items().get(1);
        assertThat(second.title()).isEqualTo("Concierto B");
        assertThat(second.venue()).isNull();
        assertThat(second.externalId()).isEqualTo("/e/2");
    }

    @Test
    void supports_scraperWithItemSelectorAndNotTuBoleta() {
        Provider generic = Provider.builder()
                .name("OtraTicketera").providerType(ProviderType.SCRAPER).config(CONFIG).build();
        Provider tuboleta = Provider.builder()
                .name("TuBoleta").providerType(ProviderType.SCRAPER).config(CONFIG).build();
        Provider api = Provider.builder()
                .name("OtraTicketera").providerType(ProviderType.API).config(CONFIG).build();
        Provider noConfig = Provider.builder()
                .name("OtraTicketera").providerType(ProviderType.SCRAPER).config(null).build();

        assertThat(extractor.supports(generic)).isTrue();
        assertThat(extractor.supports(tuboleta)).isFalse();
        assertThat(extractor.supports(api)).isFalse();
        assertThat(extractor.supports(noConfig)).isFalse();
    }
}
