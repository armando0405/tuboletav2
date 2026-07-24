package com.tuboleta.backend.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Sirve el frontend Vue (ya compilado) empaquetado en
 * {@code classpath:/static/} desde el MISMO servicio que la API — despliegue
 * "todo en uno" (un solo origen, sin CORS ni cookies cross-site).
 *
 * <p>El router de Vue usa history mode: rutas como {@code /home} o
 * {@code /admin/fuentes} no existen como archivo. El resolver devuelve el
 * archivo real cuando existe (index.html, /assets/**, favicon) y, cuando no,
 * cae a {@code index.html} para que el router del SPA resuelva la ruta. Las
 * peticiones {@code /api/**} las atienden los {@code @RestController} (tienen
 * prioridad sobre este handler de recursos), así que nunca llegan aquí.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    private static final ClassPathResource INDEX = new ClassPathResource("/static/index.html");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // Ruta del SPA (no es un archivo real): devolver el shell.
                        return INDEX;
                    }
                });
    }
}
