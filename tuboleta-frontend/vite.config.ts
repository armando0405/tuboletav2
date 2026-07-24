import { fileURLToPath, URL } from 'url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vuetify from 'vite-plugin-vuetify'
import { visualizer } from 'rollup-plugin-visualizer'

export default defineConfig({
    plugins: [
        vue(),
        visualizer({
            // En CI/Docker (CI=true) no hay navegador que abrir; en local sigue
            // abriendo el reporte de bundle tras `npm run build`.
            open: !process.env.CI,
            filename: 'dist/stats.html',
            gzipSize: true,
            brotliSize: true,
        }),
        vuetify({
            autoImport: true,
        }),
    ],

    server: {
        port: 7075,
        strictPort: true,
        host: true,
        // El frontend llama a rutas relativas '/api/**' (VITE_API_URL=/api);
        // en desarrollo se reenvian al backend Spring Boot (:8088), asi todo
        // queda mismo-origen y la cookie de sesion viaja sin lios de CORS.
        proxy: {
            '/api': {
                target: 'http://localhost:8088',
                changeOrigin: true,
            },
        },
    },

    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
    css: {
        preprocessorOptions: {
            scss: {},
        },
    },
    optimizeDeps: {
        exclude: ['vuetify'],
        entries: ['./src/**/*.vue'],
    },
})
