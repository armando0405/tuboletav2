import { fileURLToPath, URL } from 'url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vuetify from 'vite-plugin-vuetify'
import { visualizer } from 'rollup-plugin-visualizer'

export default defineConfig({
    plugins: [
        vue(),
        visualizer({
            open: true,
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
