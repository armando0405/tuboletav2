package com.tuboleta.backend.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuboleta.backend.domain.entities.NotificationLog;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.ProviderRun;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.repository.NotificationLogRepository;
import com.tuboleta.backend.repository.ProviderRepository;
import com.tuboleta.backend.repository.ProviderRunRepository;
import com.tuboleta.backend.repository.SearchProviderRepository;
import com.tuboleta.backend.service.extraction.ExtractionResult;
import com.tuboleta.backend.service.extraction.ProviderExtractor;
import com.tuboleta.backend.service.extraction.RawEventData;
import com.tuboleta.backend.service.scheduler.DueGroup;
import com.tuboleta.backend.service.scheduler.DueWorkSelector;
import com.tuboleta.backend.service.scheduler.MonitoringRunService;
import com.tuboleta.backend.utils.text.TermNormalizer;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Suite E2E (Task 8): recorre por HTTP real el recorrido completo del
 * usuario contra un contexto Spring COMPLETO (Flyway migra, Hibernate
 * {@code validate} pasa, beans del scheduler y {@code AdminBootstrapRunner}
 * cablean) y Postgres real. Deliberadamente SIN {@code @Transactional}: cada
 * corrida deja commits reales, aislados de otras corridas por un
 * email/término únicos por timestamp — no se necesita rollback ni limpieza.
 *
 * <p>El paso central es disparar una corrida de monitoreo determinista SIN
 * golpear tuboleta.com: se sustituye el único {@link ProviderExtractor} real
 * (el scraper de TuBoleta) por un doble Mockito que devuelve un resultado
 * fijo, y se invoca EXACTAMENTE el mismo camino que usa el
 * {@code MonitoringDispatcher} real: {@link DueWorkSelector#selectDueWork} +
 * {@link MonitoringRunService#runGroup}. Esto ejercita en runtime el fix de
 * Task 6 para la partición transaccional detect/notify vs recordRun con
 * entidades detached — si esa partición estuviera rota, este test explota
 * con {@code LazyInitializationException} o una excepción transaccional
 * equivalente en vez de un assert fallido.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(properties = {
        "scheduler.enabled=false",
        // Fuerza LoggingEmailSender (entrega simulada, success=true) sin importar
        // si el entorno/config tiene key de Mailgun: el test debe ser hermético
        // y no golpear un proveedor de correo real.
        "notifications.mailgun.api-key="
})
class EndToEndSmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private SearchProviderRepository searchProviderRepository;

    @Autowired
    private ProviderRunRepository providerRunRepository;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private DueWorkSelector dueWorkSelector;

    @Autowired
    private MonitoringRunService monitoringRunService;

    /**
     * Reemplaza el único {@link ProviderExtractor} real del contexto
     * (TuBoletaScraperExtractor) por un doble: el {@code List<ProviderExtractor>}
     * inyectado en {@link MonitoringRunService} pasa a contener solo este mock.
     */
    @MockitoBean
    private ProviderExtractor providerExtractor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void fullUserJourney_registerLoginSearchRunDetectNotifyDelete() throws Exception {
        long unique = System.currentTimeMillis();
        String email = "e2e." + unique + "@tuboleta.test";
        String term = "E2E Concierto Fixture " + unique;
        String normalizedTerm = TermNormalizer.normalize(term);

        // ---------- 1: registro ----------
        ResponseEntity<String> registerResp = postJson("/api/auth/register", null,
                Map.of("email", email, "name", "E2E Test User", "password", "clave12345"));
        assertThat(registerResp.getStatusCode().value()).isEqualTo(200);
        assertThat(codeOf(registerResp)).isEqualTo(0);

        // ---------- 2: login, captura de cookie de sesion ----------
        ResponseEntity<String> loginResp = postJson("/api/auth/login", null,
                Map.of("email", email, "password", "clave12345"));
        assertThat(loginResp.getStatusCode().value()).isEqualTo(200);
        assertThat(codeOf(loginResp)).isEqualTo(0);
        String sessionCookie = extractSessionCookie(loginResp);

        // ---------- 3: /me sin password_hash ----------
        ResponseEntity<String> meResp = getJson("/api/auth/me", sessionCookie);
        assertThat(meResp.getStatusCode().value()).isEqualTo(200);
        JsonNode meBody = objectMapper.readTree(meResp.getBody());
        assertThat(meBody.get("object").get("email").asText()).isEqualTo(email);
        assertThat(meBody.get("object").get("role").asText()).isEqualTo("USER");
        assertThat(meBody.get("object").has("passwordHash")).isFalse();
        assertThat(meResp.getBody().toLowerCase()).doesNotContain("password");

        // ---------- 4: crear destino de notificacion ----------
        ResponseEntity<String> destResp = postJson("/api/destinations", sessionCookie,
                Map.of("destination", email));
        assertThat(destResp.getStatusCode().value()).isEqualTo(200);
        JsonNode destBody = objectMapper.readTree(destResp.getBody());
        long destinationId = destBody.get("object").get("id").asLong();
        assertThat(destBody.get("object").get("isActive").asBoolean()).isTrue();

        // ---------- 5: crear busqueda (provider sembrado + destino) ----------
        Provider tuBoletaProvider = providerRepository.findAll().stream()
                .filter(p -> "TuBoleta".equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Proveedor 'TuBoleta' no sembrado por V1__init.sql"));
        long providerId = tuBoletaProvider.getId();

        ResponseEntity<String> searchResp = postJson("/api/searches", sessionCookie, Map.of(
                "term", term,
                "checkFrequencyMinutes", 1440,
                "providerIds", List.of(providerId),
                "destinationIds", List.of(destinationId)));
        assertThat(searchResp.getStatusCode().value()).isEqualTo(200);
        JsonNode searchBody = objectMapper.readTree(searchResp.getBody());
        long searchId = searchBody.get("object").get("id").asLong();
        assertThat(searchBody.get("object").get("term").asText()).isEqualTo(term);

        // ---------- 6: disparar una corrida de monitoreo determinista ----------
        // (mismo camino que MonitoringDispatcher.tick(): selectDueWork + runGroup,
        // pero con el extractor real sustituido por un doble fijo — nunca se
        // golpea tuboleta.com desde un test).
        RawEventData ev1 = new RawEventData(
                "e2e-fixture-1-" + unique, term + " En Vivo", "Movistar Arena", "Bogota", "5 Jul",
                "{\"href\":\"e2e-fixture-1\"}");
        RawEventData ev2 = new RawEventData(
                "e2e-fixture-2-" + unique, term + " Acustico", "Coliseo Live", "Medellin", "10 Ago",
                "{\"href\":\"e2e-fixture-2\"}");
        ExtractionResult fixtureResult = new ExtractionResult(List.of(ev1, ev2), 0);
        when(providerExtractor.supports(any())).thenReturn(true);
        when(providerExtractor.extract(any(), any())).thenReturn(fixtureResult);

        List<DueGroup> dueWork = dueWorkSelector.selectDueWork(Instant.now());
        DueGroup ourGroup = dueWork.stream()
                .filter(g -> g.provider().getId().equals(providerId) && normalizedTerm.equals(g.termNormalized()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "El par recien creado no aparecio como trabajo vencido del scheduler (selectDueWork)"));
        assertThat(ourGroup.pairs()).hasSize(1);

        // Ejercita el fix de Task 6: extraccion fuera de transaccion (aqui, un
        // mock sincrono) + detect/notify transaccional por par +
        // recordRun/last_run_at transaccional, con las entidades detached de
        // selectDueWork cruzando hacia MonitoringPersistenceService.
        monitoringRunService.runGroup(ourGroup);

        // ---------- 7: eventos detectados (NEW) ----------
        ResponseEntity<String> eventsResp = getJson("/api/searches/" + searchId + "/events", sessionCookie);
        assertThat(eventsResp.getStatusCode().value()).isEqualTo(200);
        JsonNode eventsList = objectMapper.readTree(eventsResp.getBody()).get("list");
        assertThat(eventsList).hasSize(2);
        for (JsonNode eventNode : eventsList) {
            assertThat(eventNode.get("providerName").asText()).isEqualTo("TuBoleta");
            assertThat(eventNode.get("status").asText()).isEqualTo("ACTIVE");
            assertThat(eventNode.get("title").asText()).contains(term);
        }

        // ---------- 8: inbox + unread-count ----------
        ResponseEntity<String> notifResp = getJson("/api/notifications", sessionCookie);
        assertThat(notifResp.getStatusCode().value()).isEqualTo(200);
        JsonNode notifList = objectMapper.readTree(notifResp.getBody()).get("list");
        assertThat(notifList).hasSize(2);
        for (JsonNode n : notifList) {
            assertThat(n.get("type").asText()).isEqualTo("NEW");
            assertThat(n.get("searchTerm").asText()).isEqualTo(term);
            assertThat(n.get("readAt").isNull()).isTrue();
        }

        ResponseEntity<String> unreadBeforeResp = getJson("/api/notifications/unread-count", sessionCookie);
        long unreadBefore = objectMapper.readTree(unreadBeforeResp.getBody()).get("object").asLong();
        assertThat(unreadBefore).isEqualTo(2);

        // El correo se "envia" via LoggingEmailSender (sin MAIL_API_KEY):
        // no hay entrega real, pero notifications_log queda igual, con
        // success=true (REQ-NOT-005).
        List<NotificationLog> logsForThisEmail = notificationLogRepository.findAll().stream()
                .filter(l -> email.equals(l.getDestination()))
                .toList();
        assertThat(logsForThisEmail).hasSize(2);
        assertThat(logsForThisEmail).allSatisfy(l -> assertThat(l.getSuccess()).isTrue());

        // ---------- 9: marcar una notificacion como leida ----------
        long firstNotificationId = notifList.get(0).get("id").asLong();
        ResponseEntity<String> markReadResp = postJson(
                "/api/notifications/" + firstNotificationId + "/read", sessionCookie, null);
        assertThat(markReadResp.getStatusCode().value()).isEqualTo(200);
        assertThat(codeOf(markReadResp)).isEqualTo(0);

        ResponseEntity<String> unreadAfterResp = getJson("/api/notifications/unread-count", sessionCookie);
        long unreadAfter = objectMapper.readTree(unreadAfterResp.getBody()).get("object").asLong();
        assertThat(unreadAfter).isEqualTo(unreadBefore - 1);

        // ---------- 10: borrado logico de la busqueda ----------
        ResponseEntity<String> deleteResp = deleteRequest("/api/searches/" + searchId, sessionCookie);
        assertThat(deleteResp.getStatusCode().value()).isEqualTo(200);
        assertThat(codeOf(deleteResp)).isEqualTo(0);

        ResponseEntity<String> listAfterDeleteResp = getJson("/api/searches", sessionCookie);
        JsonNode searchesAfterDelete = objectMapper.readTree(listAfterDeleteResp.getBody()).get("list");
        boolean stillListed = false;
        for (JsonNode s : searchesAfterDelete) {
            if (s.get("id").asLong() == searchId) {
                stillListed = true;
            }
        }
        assertThat(stillListed).isFalse();

        // ---------- 11: provider_runs + last_run_at del par ----------
        List<ProviderRun> matchingRuns = providerRunRepository.findAll().stream()
                .filter(r -> normalizedTerm.equals(r.getTermNormalized()))
                .toList();
        assertThat(matchingRuns).hasSize(1);
        ProviderRun run = matchingRuns.get(0);
        assertThat(run.getSuccess()).isTrue();
        assertThat(run.getErrorMessage()).isNull();
        assertThat(run.getEventsFound()).isEqualTo(2);
        assertThat(run.getPairsApplied()).isEqualTo(1);

        SearchProvider pair = searchProviderRepository.findBySearchIdAndProviderId(searchId, providerId)
                .orElseThrow();
        assertThat(pair.getLastRunAt()).isNotNull();
    }

    // ---------- helpers HTTP (TestRestTemplate no mantiene cookies entre
    // llamadas: la cookie de sesion se captura del login y se reenvia a mano) ----------

    private ResponseEntity<String> postJson(String path, String sessionCookie, Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (sessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, sessionCookie);
        }
        String json = body == null ? "" : objectMapper.writeValueAsString(body);
        return restTemplate.exchange(url(path), HttpMethod.POST, new HttpEntity<>(json, headers), String.class);
    }

    private ResponseEntity<String> getJson(String path, String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, sessionCookie);
        }
        return restTemplate.exchange(url(path), HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    private ResponseEntity<String> deleteRequest(String path, String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, sessionCookie);
        }
        return restTemplate.exchange(url(path), HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String extractSessionCookie(ResponseEntity<String> response) {
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).as("El login debe devolver Set-Cookie de sesion").isNotNull().isNotEmpty();
        return setCookies.get(0).split(";", 2)[0];
    }

    private int codeOf(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody()).get("code").asInt();
    }
}
