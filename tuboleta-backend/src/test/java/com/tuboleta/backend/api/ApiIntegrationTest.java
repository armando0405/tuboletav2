package com.tuboleta.backend.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuboleta.backend.domain.entities.Event;
import com.tuboleta.backend.domain.entities.Notification;
import com.tuboleta.backend.domain.entities.Provider;
import com.tuboleta.backend.domain.entities.Search;
import com.tuboleta.backend.domain.entities.SearchProvider;
import com.tuboleta.backend.domain.entities.User;
import com.tuboleta.backend.domain.enums.NotificationType;
import com.tuboleta.backend.domain.enums.SearchStatus;
import com.tuboleta.backend.domain.enums.UserRole;
import com.tuboleta.backend.domain.enums.UserStatus;
import com.tuboleta.backend.repository.EventRepository;
import com.tuboleta.backend.repository.NotificationRepository;
import com.tuboleta.backend.repository.ProviderRepository;
import com.tuboleta.backend.repository.SearchNotificationRepository;
import com.tuboleta.backend.repository.SearchProviderRepository;
import com.tuboleta.backend.repository.SearchRepository;
import com.tuboleta.backend.repository.UserRepository;
import com.tuboleta.backend.service.NotificationService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Suite de integración de Task 7 (API REST + seguridad): un caso por
 * escenario del brief, contra Postgres real (Flyway ya aplicado) y la
 * cadena completa de Spring Security (sesión de servidor, envelope JSON en
 * 401/403). {@code @Transactional} en la clase hace rollback por método.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "scheduler.enabled=false")
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private SearchProviderRepository searchProviderRepository;

    @Autowired
    private SearchNotificationRepository searchNotificationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EventRepository eventRepository;

    @MockitoSpyBean
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------- helpers ----------

    private MockHttpSession registerAndLogin(String email, String name, String password) throws Exception {
        Map<String, Object> registerBody = Map.of(
                "email", email,
                "name", name,
                "password", password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MockHttpSession session = new MockHttpSession();
        Map<String, Object> loginBody = Map.of("email", email, "password", password);
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        return session;
    }

    private MockHttpSession loginAsAdmin() throws Exception {
        String email = "admin.test@tuboleta.local";
        String rawPassword = "admin12345";
        userRepository.findByEmail(email).orElseGet(() -> userRepository.save(User.builder()
                .email(email)
                .name("Admin de prueba")
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build()));

        MockHttpSession session = new MockHttpSession();
        Map<String, Object> loginBody = Map.of("email", email, "password", rawPassword);
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        return session;
    }

    private Long createDestination(MockHttpSession session, String destinationEmail) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/destinations")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("destination", destinationEmail))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("object").get("id").asLong();
    }

    private Long tuBoletaProviderId() {
        return providerRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay proveedores sembrados por V1__init.sql"))
                .getId();
    }

    // ---------- 1: register -> login -> me ----------

    @Test
    void registerLoginMe_happyPath() throws Exception {
        String email = "juan.perez@example.com";
        MockHttpSession session = registerAndLogin(email, "Juan Perez", "clave12345");

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.object.email").value(email))
                .andExpect(jsonPath("$.object.role").value("USER"));
    }

    // ---------- 2: login con password malo -> 401 envelope ----------

    @Test
    void login_wrongPassword_returns401Envelope() throws Exception {
        String email = "maria.lopez@example.com";
        registerAndLogin(email, "Maria Lopez", "clave12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", "otraClave123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(-1));
    }

    // ---------- 2b: registro sin password -> 400 (validacion, no 500) ----------

    @Test
    void register_missingPassword_returns400() throws Exception {
        Map<String, Object> registerBody = new java.util.HashMap<>();
        registerBody.put("email", "sin.password@example.com");
        registerBody.put("name", "Sin Password");
        registerBody.put("password", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(-1));
    }

    // ---------- 3: endpoint protegido sin sesion -> 401 envelope ----------

    @Test
    void protectedEndpoint_withoutSession_returns401Envelope() throws Exception {
        mockMvc.perform(get("/api/searches"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(-1));
    }

    // ---------- 4: crear busqueda con destino y proveedor -> 200 y filas creadas ----------

    @Test
    void createSearch_withDestinationAndProvider_persistsRows() throws Exception {
        MockHttpSession session = registerAndLogin("carlos.ruiz@example.com", "Carlos Ruiz", "clave12345");
        Long destinationId = createDestination(session, "carlos.ruiz@example.com");
        Long providerId = tuBoletaProviderId();

        Map<String, Object> body = Map.of(
                "term", "Karol G",
                "checkFrequencyMinutes", 1440,
                "providerIds", java.util.List.of(providerId),
                "destinationIds", java.util.List.of(destinationId));

        MvcResult result = mockMvc.perform(post("/api/searches")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.object.term").value("Karol G"))
                .andReturn();

        Long searchId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("object").get("id").asLong();

        Search persisted = searchRepository.findById(searchId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(SearchStatus.ACTIVE);
        assertThat(searchProviderRepository.findBySearchId(searchId)).hasSize(1);
        assertThat(searchNotificationRepository.findBySearchId(searchId)).hasSize(1);
    }

    // ---------- 5: busqueda duplicada (termino normalizado equivalente) -> error de negocio ----------

    @Test
    void createSearch_duplicateNormalizedTerm_isBusinessError() throws Exception {
        MockHttpSession session = registerAndLogin("ana.gomez@example.com", "Ana Gomez", "clave12345");
        Long providerId = tuBoletaProviderId();

        Map<String, Object> first = Map.of(
                "term", "Bad Bunny",
                "checkFrequencyMinutes", 1440,
                "providerIds", java.util.List.of(providerId),
                "destinationIds", java.util.List.of());
        mockMvc.perform(post("/api/searches")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isOk());

        Map<String, Object> duplicate = Map.of(
                "term", "  BAD   bunny  ",
                "checkFrequencyMinutes", 720,
                "providerIds", java.util.List.of(providerId),
                "destinationIds", java.util.List.of());
        mockMvc.perform(post("/api/searches")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(-1));
    }

    // ---------- 6: frecuencia invalida (ej. 7) -> 400 ----------

    @Test
    void createSearch_invalidFrequency_returns400() throws Exception {
        MockHttpSession session = registerAndLogin("pedro.diaz@example.com", "Pedro Diaz", "clave12345");
        Long providerId = tuBoletaProviderId();

        Map<String, Object> body = Map.of(
                "term", "Shakira",
                "checkFrequencyMinutes", 7,
                "providerIds", java.util.List.of(providerId),
                "destinationIds", java.util.List.of());

        mockMvc.perform(post("/api/searches")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(-1));
    }

    // ---------- 7: marcar notificacion leida de OTRO usuario -> not found ----------

    @Test
    void markRead_notificationOfAnotherUser_isNotFound() throws Exception {
        // Usuario B: dueño de la notificacion.
        User userB = userRepository.save(User.builder()
                .email("dueno.b@example.com")
                .name("Dueno B")
                .passwordHash(passwordEncoder.encode("claveB12345"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());
        Search searchB = searchRepository.save(Search.builder()
                .user(userB)
                .term("Coldplay")
                .termNormalized("coldplay")
                .checkFrequencyMinutes(1440)
                .status(SearchStatus.ACTIVE)
                .build());
        Notification notificationB = notificationRepository.save(Notification.builder()
                .user(userB)
                .search(searchB)
                .type(NotificationType.NEW)
                .build());

        // Usuario A: intenta marcar como leida la notificacion de B.
        MockHttpSession sessionA = registerAndLogin("intruso.a@example.com", "Intruso A", "clave12345");

        mockMvc.perform(post("/api/notifications/" + notificationB.getId() + "/read").session(sessionA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(-1));

        Notification stillUnread = notificationRepository.findById(notificationB.getId()).orElseThrow();
        assertThat(stillUnread.getReadAt()).isNull();
    }

    // ---------- 8: /api/admin/** con USER -> 403 envelope ----------

    @Test
    void adminEndpoint_withUserRole_returns403Envelope() throws Exception {
        MockHttpSession session = registerAndLogin("usuario.raso@example.com", "Usuario Raso", "clave12345");

        mockMvc.perform(get("/api/admin/providers").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(-1));
    }

    // ---------- 9: disable provider como ADMIN -> DISABLED + notifyProviderDisabled ----------

    @Test
    void disableProvider_asAdmin_setsDisabledAndNotifies() throws Exception {
        MockHttpSession adminSession = loginAsAdmin();
        Long providerId = tuBoletaProviderId();

        mockMvc.perform(post("/api/admin/providers/" + providerId + "/disable")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Mantenimiento programado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.object.status").value("DISABLED"))
                .andExpect(jsonPath("$.object.statusReason").value("Mantenimiento programado"));

        Provider persisted = providerRepository.findById(providerId).orElseThrow();
        assertThat(persisted.getStatus().name()).isEqualTo("DISABLED");
        assertThat(persisted.getStatusReason()).isEqualTo("Mantenimiento programado");

        verify(notificationService).notifyProviderDisabled(any(), eq("Mantenimiento programado"));
    }

    // ---------- 10: DELETE busqueda -> DELETED y no aparece en el GET ----------

    @Test
    void deleteSearch_marksDeletedAndHidesFromList() throws Exception {
        MockHttpSession session = registerAndLogin("laura.torres@example.com", "Laura Torres", "clave12345");
        Long providerId = tuBoletaProviderId();

        Map<String, Object> body = Map.of(
                "term", "Feid",
                "checkFrequencyMinutes", 1440,
                "providerIds", java.util.List.of(providerId),
                "destinationIds", java.util.List.of());
        MvcResult result = mockMvc.perform(post("/api/searches")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        Long searchId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("object").get("id").asLong();

        mockMvc.perform(delete("/api/searches/" + searchId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Search persisted = searchRepository.findById(searchId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(SearchStatus.DELETED);

        mockMvc.perform(get("/api/searches").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[?(@.id == " + searchId + ")]").isEmpty());
    }

    // ---------- 11: toggle de busqueda completa ACTIVE<->INACTIVE + ownership ----------

    @Test
    void toggleSearchStatus_flipsActiveInactive_andOwnershipIsEnforced() throws Exception {
        MockHttpSession session = registerAndLogin("mario.cano@example.com", "Mario Cano", "clave12345");
        Long providerId = tuBoletaProviderId();

        Map<String, Object> body = Map.of(
                "term", "Aterciopelados",
                "checkFrequencyMinutes", 1440,
                "providerIds", java.util.List.of(providerId),
                "destinationIds", java.util.List.of());
        MvcResult result = mockMvc.perform(post("/api/searches")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        Long searchId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("object").get("id").asLong();

        mockMvc.perform(patch("/api/searches/" + searchId + "/toggle").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.object.status").value("INACTIVE"));
        assertThat(searchRepository.findById(searchId).orElseThrow().getStatus()).isEqualTo(SearchStatus.INACTIVE);

        mockMvc.perform(patch("/api/searches/" + searchId + "/toggle").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object.status").value("ACTIVE"));
        assertThat(searchRepository.findById(searchId).orElseThrow().getStatus()).isEqualTo(SearchStatus.ACTIVE);

        MockHttpSession intruderSession = registerAndLogin("intruso.b@example.com", "Intruso B", "clave12345");
        mockMvc.perform(patch("/api/searches/" + searchId + "/toggle").session(intruderSession))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(-1));

        // La busqueda de Mario sigue intacta (el intento del intruso no la toco).
        assertThat(searchRepository.findById(searchId).orElseThrow().getStatus()).isEqualTo(SearchStatus.ACTIVE);
    }

    // ---------- 12: SearchResponse trae destinationIds y eventsCount ----------

    @Test
    void searchResponse_includesDestinationIdsAndEventsCount() throws Exception {
        MockHttpSession session = registerAndLogin("nora.diaz@example.com", "Nora Diaz", "clave12345");
        Long destinationId = createDestination(session, "nora.diaz@example.com");
        Long providerId = tuBoletaProviderId();

        Map<String, Object> body = Map.of(
                "term", "Fonseca",
                "checkFrequencyMinutes", 1440,
                "providerIds", java.util.List.of(providerId),
                "destinationIds", java.util.List.of(destinationId));
        MvcResult createResult = mockMvc.perform(post("/api/searches")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object.destinationIds[0]").value(destinationId))
                .andExpect(jsonPath("$.object.eventsCount").value(0))
                .andReturn();

        Long searchId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("object").get("id").asLong();

        SearchProvider pair = searchProviderRepository.findBySearchId(searchId).get(0);
        eventRepository.save(Event.builder()
                .searchProvider(pair)
                .externalId("ext-1")
                .title("Concierto Fonseca")
                .build());
        eventRepository.save(Event.builder()
                .searchProvider(pair)
                .externalId("ext-2")
                .title("Concierto Fonseca 2")
                .build());

        MvcResult listResult = mockMvc.perform(get("/api/searches").session(session))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode list = objectMapper.readTree(listResult.getResponse().getContentAsString()).get("list");
        JsonNode found = null;
        for (JsonNode node : list) {
            if (node.get("id").asLong() == searchId) {
                found = node;
            }
        }
        assertThat(found).isNotNull();
        assertThat(found.get("eventsCount").asLong()).isEqualTo(2L);
        assertThat(found.get("destinationIds").get(0).asLong()).isEqualTo(destinationId);
    }

    // ---------- 13: providerStatusReason poblado cuando el proveedor esta DISABLED ----------

    @Test
    void searchResponse_includesProviderStatusReason_whenProviderDisabled() throws Exception {
        MockHttpSession adminSession = loginAsAdmin();
        Long providerId = tuBoletaProviderId();
        mockMvc.perform(post("/api/admin/providers/" + providerId + "/disable")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Sitio caido por mantenimiento"))))
                .andExpect(status().isOk());

        MockHttpSession session = registerAndLogin("silvia.rey@example.com", "Silvia Rey", "clave12345");
        Map<String, Object> body = Map.of(
                "term", "Morat",
                "checkFrequencyMinutes", 1440,
                "providerIds", java.util.List.of(providerId),
                "destinationIds", java.util.List.of());

        mockMvc.perform(post("/api/searches")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object.providers[0].providerStatus").value("DISABLED"))
                .andExpect(jsonPath("$.object.providers[0].providerStatusReason").value("Sitio caido por mantenimiento"));
    }
}
