package com.tuboleta.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * GET /api/providers (REQ-FUE-001): catálogo público de fuentes ACTIVE, para
 * cualquier usuario autenticado al elegir proveedor(es) en una búsqueda.
 * Distinto de {@code /api/admin/providers}, restringido a ADMIN.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "scheduler.enabled=false")
@Transactional
class ProviderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    // ---------- 1: usuario autenticado -> 200, catalogo con proveedores ACTIVE ----------

    @Test
    void listProviders_authenticated_returnsActiveProviders() throws Exception {
        MockHttpSession session = registerAndLogin("nueva.busqueda@example.com", "Nueva Busqueda", "clave12345");

        mockMvc.perform(get("/api/providers").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.list[?(@.name == 'TuBoleta')]").exists())
                .andExpect(jsonPath("$.list[0].id").exists())
                .andExpect(jsonPath("$.list[0].name").exists())
                .andExpect(jsonPath("$.list[0].type").exists());
    }

    // ---------- 2: sin sesion -> 401 envelope ----------

    @Test
    void listProviders_withoutSession_returns401Envelope() throws Exception {
        mockMvc.perform(get("/api/providers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(-1));
    }
}
