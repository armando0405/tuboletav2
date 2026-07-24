package com.tuboleta.backend.service.extraction;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.enums.ProviderType;
import org.junit.jupiter.api.Test;

class ApiProviderExtractorTest {

    private final ApiProviderExtractor extractor = new ApiProviderExtractor();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parse_extractsFromNestedItemsPath() throws Exception {
        String body = """
                {"data":{"events":[
                    {"id":"e1","name":"Show A","place":"Arena","city":"Bogotá","when":"5 Jul"},
                    {"id":"e2","name":"Show B"}
                ]}}""";
        JsonNode config = mapper.readTree("""
                {"items_path":"data.events","id_field":"id","title_field":"name",
                 "venue_field":"place","city_field":"city","date_field":"when"}""");

        ExtractionResult result = extractor.parse(body, config);

        assertThat(result.items()).hasSize(2);
        assertThat(result.failedItems()).isZero();
        RawEventData first = result.items().get(0);
        assertThat(first.externalId()).isEqualTo("e1");
        assertThat(first.title()).isEqualTo("Show A");
        assertThat(first.venue()).isEqualTo("Arena");
        assertThat(first.eventDateRaw()).isEqualTo("5 Jul");
        RawEventData second = result.items().get(1);
        assertThat(second.externalId()).isEqualTo("e2");
        assertThat(second.venue()).isNull();
    }

    @Test
    void parse_rootArrayWhenItemsPathEmpty() throws Exception {
        String body = """
                [{"id":"x","name":"Uno"},{"id":"y","name":"Dos"}]""";
        JsonNode config = mapper.readTree("""
                {"id_field":"id","title_field":"name"}""");

        ExtractionResult result = extractor.parse(body, config);

        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).externalId()).isEqualTo("x");
        assertThat(result.items().get(1).title()).isEqualTo("Dos");
    }

    @Test
    void supports_onlyApiProviders() {
        Provider api = Provider.builder().name("Feria").providerType(ProviderType.API).build();
        Provider scraper = Provider.builder().name("Feria").providerType(ProviderType.SCRAPER).build();

        assertThat(extractor.supports(api)).isTrue();
        assertThat(extractor.supports(scraper)).isFalse();
    }
}
