-- V2 — Corrige la URL de búsqueda del proveedor TuBoleta.
--
-- La URL inicial (/es/callback/search-results?s={term}) no es la ruta real de
-- resultados: no devuelve el HTML del listado. La correcta es la de resultados
-- de búsqueda con sus filtros por defecto. El placeholder {term} lo reemplaza
-- TuBoletaScraperExtractor con el término normalizado ya URL-encoded (los
-- espacios quedan como '+', ej. "fucks news" -> "fucks+news").
--
-- Se hace como migración incremental (no editando V1) para no romper el
-- checksum de Flyway en bases ya inicializadas ni obligar a recrear la BD.

UPDATE providers
SET search_url = 'https://www.tuboleta.com/es/resultados-de-busqueda?ciudades=All&categorias=All&fecha_inicio=&fecha_final=&s={term}'
WHERE name = 'TuBoleta'
  AND search_url = 'https://www.tuboleta.com/es/callback/search-results?s={term}';
