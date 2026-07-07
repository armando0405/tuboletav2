<template>
    <div class="d-flex align-center ga-1 provider-chip-wrap">
        <v-tooltip
            :text="reasonTooltip"
            :disabled="!reasonTooltip"
        >
            <template #activator="{ props: activatorProps }">
                <v-chip
                    v-bind="activatorProps"
                    :color="effective.color"
                    variant="tonal"
                    size="small"
                    :prepend-icon="effective.icon"
                    class="font-weight-medium"
                >
                    {{ provider.providerName }}
                    <span class="text-caption ml-1 opacity-80">· {{ effective.label }}</span>
                </v-chip>
            </template>
        </v-tooltip>

        <v-tooltip :text="toggleTooltip">
            <template #activator="{ props: activatorProps }">
                <v-btn
                    v-bind="activatorProps"
                    icon
                    size="x-small"
                    variant="text"
                    :disabled="provider.providerStatus === 'DISABLED'"
                    :aria-label="toggleTooltip"
                    @click="emit('toggle')"
                >
                    <v-icon
                        :icon="provider.pairActive ? 'mdi-pause' : 'mdi-play'"
                        size="18"
                    />
                </v-btn>
            </template>
        </v-tooltip>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { SearchProviderInfo } from '@/types/services/Search'

const props = defineProps<{
    provider: SearchProviderInfo
}>()

const emit = defineEmits<{
    toggle: []
}>()

// Estado EFECTIVO del par (REQ-FE-002): combina pairActive (decisión del
// usuario) + providerStatus (decisión del ADMIN). El backend solo entrega los
// datos crudos — esta es la única fuente de la regla de negocio en el front.
type EffectiveState = {
    label: string
    color: 'success' | 'warning' | 'error'
    icon: string
}

const effective = computed<EffectiveState>(() => {
    if (props.provider.providerStatus === 'DISABLED') {
        return { label: 'Fuente deshabilitada', color: 'error', icon: 'mdi-alert-circle-outline' }
    }
    if (!props.provider.pairActive) {
        return { label: 'Pausado', color: 'warning', icon: 'mdi-pause-circle-outline' }
    }
    return { label: 'Activo', color: 'success', icon: 'mdi-check-circle-outline' }
})

const toggleTooltip = computed<string>(() => {
    if (props.provider.providerStatus === 'DISABLED') {
        return 'Deshabilitado por el administrador: no se puede pausar/reanudar'
    }
    return props.provider.pairActive ? 'Pausar este proveedor' : 'Reanudar este proveedor'
})

// Motivo de la deshabilitación (REQ-FUE-002), visible en el detalle de la
// búsqueda afectada. Vacío (tooltip deshabilitado) cuando la fuente está
// activa o el ADMIN no dejó un motivo.
const reasonTooltip = computed<string>(() => {
    if (props.provider.providerStatus === 'DISABLED' && props.provider.providerStatusReason) {
        return `Motivo: ${props.provider.providerStatusReason}`
    }
    return ''
})
</script>

<style scoped>
.provider-chip-wrap {
    border: 1px solid rgb(var(--v-theme-outline));
    border-radius: 999px;
    padding-right: 2px;
}
</style>
