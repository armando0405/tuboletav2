-- ============================================================
-- V4 — Renombrado de tablas y columnas a español.
-- ============================================================
-- Migración incremental (no se editan V1/V2/V3, ya aplicadas en la BD
-- compartida): romperían el checksum de Flyway.
-- Solo se renombran TABLAS y COLUMNAS. Los VALORES de columnas enum
-- (ej. 'ACTIVE', 'SCRAPER', 'NEW') NO cambian. Postgres preserva datos,
-- FKs e índices automáticamente en un RENAME.
-- Las entidades JPA se actualizan en el mismo cambio con
-- @Table(name=...) / @Column(name=...) / @JoinColumn(name=...); los
-- nombres de campo Java, los DTOs y el frontend no se tocan.
-- ============================================================


-- ============================================================
-- providers → proveedores
-- ============================================================
ALTER TABLE providers RENAME COLUMN name TO nombre;
ALTER TABLE providers RENAME COLUMN provider_type TO tipo_proveedor;
ALTER TABLE providers RENAME COLUMN base_url TO url_base;
ALTER TABLE providers RENAME COLUMN search_url TO url_busqueda;
ALTER TABLE providers RENAME COLUMN status TO estado;
ALTER TABLE providers RENAME COLUMN status_reason TO motivo_estado;
ALTER TABLE providers RENAME COLUMN status_changed_at TO estado_cambiado_en;
ALTER TABLE providers RENAME COLUMN created_at TO creado_en;
ALTER TABLE providers RENAME COLUMN updated_at TO actualizado_en;
ALTER TABLE providers RENAME TO proveedores;

-- ============================================================
-- notif_channels → canales_notificacion
-- ============================================================
ALTER TABLE notif_channels RENAME COLUMN name TO nombre;
ALTER TABLE notif_channels RENAME COLUMN is_active TO activo;
ALTER TABLE notif_channels RENAME TO canales_notificacion;

-- ============================================================
-- users → usuarios
-- ============================================================
ALTER TABLE users RENAME COLUMN email TO correo;
ALTER TABLE users RENAME COLUMN name TO nombre;
ALTER TABLE users RENAME COLUMN password_hash TO clave_hash;
ALTER TABLE users RENAME COLUMN role TO rol;
ALTER TABLE users RENAME COLUMN status TO estado;
ALTER TABLE users RENAME COLUMN created_at TO creado_en;
ALTER TABLE users RENAME TO usuarios;

-- ============================================================
-- user_notification_channels → destinos_usuario
-- ============================================================
ALTER TABLE user_notification_channels RENAME COLUMN user_id TO usuario_id;
ALTER TABLE user_notification_channels RENAME COLUMN channel_id TO canal_id;
ALTER TABLE user_notification_channels RENAME COLUMN destination TO destino;
ALTER TABLE user_notification_channels RENAME COLUMN is_active TO activo;
ALTER TABLE user_notification_channels RENAME COLUMN created_at TO creado_en;
ALTER TABLE user_notification_channels RENAME TO destinos_usuario;

-- ============================================================
-- searches → busquedas
-- ============================================================
ALTER TABLE searches RENAME COLUMN user_id TO usuario_id;
ALTER TABLE searches RENAME COLUMN term TO termino;
ALTER TABLE searches RENAME COLUMN term_normalized TO termino_normalizado;
ALTER TABLE searches RENAME COLUMN check_frequency_minutes TO frecuencia_minutos;
ALTER TABLE searches RENAME COLUMN status TO estado;
ALTER TABLE searches RENAME COLUMN created_at TO creado_en;
ALTER TABLE searches RENAME TO busquedas;

-- ============================================================
-- search_providers → busqueda_proveedores
-- ============================================================
ALTER TABLE search_providers RENAME COLUMN search_id TO busqueda_id;
ALTER TABLE search_providers RENAME COLUMN provider_id TO proveedor_id;
ALTER TABLE search_providers RENAME COLUMN is_active TO activo;
ALTER TABLE search_providers RENAME COLUMN last_run_at TO ultima_corrida_en;
ALTER TABLE search_providers RENAME COLUMN created_at TO creado_en;
ALTER TABLE search_providers RENAME TO busqueda_proveedores;

-- ============================================================
-- provider_runs → corridas_proveedor
-- ============================================================
ALTER TABLE provider_runs RENAME COLUMN provider_id TO proveedor_id;
ALTER TABLE provider_runs RENAME COLUMN term_normalized TO termino_normalizado;
ALTER TABLE provider_runs RENAME COLUMN started_at TO iniciado_en;
ALTER TABLE provider_runs RENAME COLUMN finished_at TO finalizado_en;
ALTER TABLE provider_runs RENAME COLUMN success TO exito;
ALTER TABLE provider_runs RENAME COLUMN error_message TO mensaje_error;
ALTER TABLE provider_runs RENAME COLUMN events_found TO eventos_encontrados;
ALTER TABLE provider_runs RENAME COLUMN pairs_applied TO pares_aplicados;
ALTER TABLE provider_runs RENAME TO corridas_proveedor;

-- ============================================================
-- search_notifications → busqueda_destinos
-- ============================================================
ALTER TABLE search_notifications RENAME COLUMN search_id TO busqueda_id;
ALTER TABLE search_notifications RENAME COLUMN user_notification_channel_id TO destino_usuario_id;
ALTER TABLE search_notifications RENAME COLUMN is_active TO activo;
ALTER TABLE search_notifications RENAME TO busqueda_destinos;

-- ============================================================
-- events → eventos
-- ============================================================
ALTER TABLE events RENAME COLUMN search_provider_id TO busqueda_proveedor_id;
ALTER TABLE events RENAME COLUMN external_id TO id_externo;
ALTER TABLE events RENAME COLUMN title TO titulo;
ALTER TABLE events RENAME COLUMN venue TO lugar;
ALTER TABLE events RENAME COLUMN event_date_raw TO fecha_evento_texto;
ALTER TABLE events RENAME COLUMN event_date TO fecha_evento;
ALTER TABLE events RENAME COLUMN raw_json TO json_crudo;
ALTER TABLE events RENAME COLUMN status TO estado;
ALTER TABLE events RENAME COLUMN miss_count TO conteo_ausencias;
ALTER TABLE events RENAME COLUMN first_seen_at TO visto_primera_vez_en;
ALTER TABLE events RENAME COLUMN last_seen_at TO visto_ultima_vez_en;
ALTER TABLE events RENAME TO eventos;

-- ============================================================
-- event_changes → cambios_evento
-- ============================================================
ALTER TABLE event_changes RENAME COLUMN event_id TO evento_id;
ALTER TABLE event_changes RENAME COLUMN field_name TO campo;
ALTER TABLE event_changes RENAME COLUMN old_value TO valor_anterior;
ALTER TABLE event_changes RENAME COLUMN new_value TO valor_nuevo;
ALTER TABLE event_changes RENAME COLUMN detected_at TO detectado_en;
ALTER TABLE event_changes RENAME TO cambios_evento;

-- ============================================================
-- notifications → notificaciones
-- ============================================================
ALTER TABLE notifications RENAME COLUMN user_id TO usuario_id;
ALTER TABLE notifications RENAME COLUMN search_id TO busqueda_id;
ALTER TABLE notifications RENAME COLUMN event_id TO evento_id;
ALTER TABLE notifications RENAME COLUMN type TO tipo;
ALTER TABLE notifications RENAME COLUMN created_at TO creado_en;
ALTER TABLE notifications RENAME COLUMN read_at TO leido_en;
ALTER TABLE notifications RENAME TO notificaciones;

-- ============================================================
-- notifications_log → notificaciones_bitacora
-- ============================================================
ALTER TABLE notifications_log RENAME COLUMN notification_id TO notificacion_id;
ALTER TABLE notifications_log RENAME COLUMN channel_id TO canal_id;
ALTER TABLE notifications_log RENAME COLUMN destination TO destino;
ALTER TABLE notifications_log RENAME COLUMN sent_at TO enviado_en;
ALTER TABLE notifications_log RENAME COLUMN success TO exito;
ALTER TABLE notifications_log RENAME TO notificaciones_bitacora;

-- ============================================================
-- frequencies → frecuencias
-- ============================================================
ALTER TABLE frequencies RENAME COLUMN label TO etiqueta;
ALTER TABLE frequencies RENAME COLUMN minutes TO minutos;
ALTER TABLE frequencies RENAME COLUMN is_active TO activo;
ALTER TABLE frequencies RENAME COLUMN created_at TO creado_en;
ALTER TABLE frequencies RENAME TO frecuencias;
