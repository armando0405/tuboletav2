<template>
    <div class="position-relative">
        <v-data-table
            v-bind="tableAttrs"
            :class="{ 'filters-active': allFiltersVisible }"
            :headers="processedHeaders"
            :items="filteredItems"
            :item-key="actualItemKey"
            v-model="selectedItems"
            fixed-header
            @click:row="handleRowClick"
            :row-props="getRowProps"
        >
            <template #top>
                <v-toolbar
                    flat
                    class="px-4"
                >
                    <v-icon
                        size="large"
                        color="primary"
                        class="m-n12"
                        @click.stop="toggleAllFilters"
                    >
                        {{ allFiltersVisible ? 'mdi-filter-off-outline' : 'mdi-filter-outline' }}
                    </v-icon>
                    <v-spacer />
                    <slot name="toolbar-actions"></slot>
                    <v-tooltip :text="t('create')">
                        <template #activator="{ props: activatorProps }">
                            <v-btn
                                v-bind="activatorProps"
                                v-if="showCreate"
                                prepend-icon="mdi-plus"
                                @click="emit('create')"
                            >
                                {{ t('create') }}
                            </v-btn>
                        </template>
                    </v-tooltip>
                </v-toolbar>
            </template>

            <template
                v-for="colHeader in processedHeaders"
                :key="colHeader.key"
                #[`header.${colHeader.key}`]
            >
                <div class="flex flex-col w-full">
                    <div class="flex justify-center items-center gap-2 w-full">
                        <span>{{ colHeader.title }}</span>
                    </div>

                    <div
                        v-if="allFiltersVisible"
                        class="filter-container"
                    >
                        <v-text-field
                            v-if="getFilterType(colHeader) === 'text'"
                            v-model="filters[colHeader.key]"
                            density="compact"
                            hide-details
                            variant="outlined"
                            clearable
                        />

                        <v-select
                            v-else-if="getFilterType(colHeader) === 'select'"
                            v-model="filters[colHeader.key]"
                            :items="getFilterItems(colHeader)"
                            density="compact"
                            hide-details
                            variant="outlined"
                            clearable
                        />

                        <v-text-field
                            v-else-if="getFilterType(colHeader) === 'date'"
                            v-model="filters[colHeader.key]"
                            type="date"
                            density="compact"
                            hide-details
                            variant="outlined"
                            clearable
                        />

                        <v-select
                            v-else-if="getFilterType(colHeader) === 'state'"
                            v-model="filters[colHeader.key]"
                            :items="getFilterItems(colHeader)"
                            density="compact"
                            hide-details
                            variant="outlined"
                            clearable
                        />
                    </div>
                </div>
            </template>

            <!-- COLUMNA STATE: ícono según valor S/N -->
            <template
                v-for="colHeader in stateHeaders"
                :key="`state-${colHeader.key}`"
                #[`item.${colHeader.key}`]="{ item }"
            >
                <div class="d-flex justify-center">
                    <v-icon
                        :color="
                            (item as Record<string, any>)[colHeader.key] === 'S'
                                ? 'success'
                                : 'error'
                        "
                        :icon="
                            (item as Record<string, any>)[colHeader.key] === 'S'
                                ? 'mdi-check-circle-outline'
                                : 'mdi-close-circle-outline'
                        "
                    />
                </div>
            </template>

            <!-- COLUMNA ACCIONES -->
            <template #[`item.actions`]="{ item }">
                <div class="d-flex ga-2 justify-center">
                    <v-tooltip :text="t('edit')">
                        <template #activator="{ props: activatorProps }">
                            <v-btn
                                v-bind="activatorProps"
                                v-if="showEdit"
                                icon
                                size="small"
                                variant="text"
                                @click.stop="handleActionRowClick('edit', item)"
                            >
                                <v-icon
                                    icon="mdi-circle-edit-outline"
                                    color="success"
                                ></v-icon>
                            </v-btn>
                        </template>
                    </v-tooltip>

                    <v-tooltip :text="t('delete')">
                        <template #activator="{ props: activatorProps }">
                            <v-btn
                                v-bind="activatorProps"
                                v-if="showDelete"
                                icon
                                size="small"
                                variant="text"
                                @click.stop="handleActionRowClick('delete', item)"
                            >
                                <v-icon
                                    icon="mdi-delete-outline"
                                    color="error"
                                ></v-icon>
                            </v-btn>
                        </template>
                    </v-tooltip>
                </div>
            </template>

            <template
                v-for="(_, name) in $slots"
                :key="name"
                #[name]="slotProps"
            >
                <slot
                    :name="name"
                    v-bind="slotProps ?? {}"
                />
            </template>
        </v-data-table>
    </div>
</template>

<script setup lang="ts">
import type { ColumnFilter, DataTableHeader, FilterType, TableHeader } from '@/types'
import { reactive, computed, useAttrs, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

type recordStringUnknown = Record<string, unknown>

const props = withDefaults(
    defineProps<{
        headers: DataTableHeader[]
        items: recordStringUnknown[]
        itemKey?: string
        selectionMode?: 'none' | 'single' | 'multiple'
        showCreate?: boolean
        showEdit?: boolean
        showDelete?: boolean
    }>(),
    {
        selectionMode: 'none',
        itemKey: '',
        showCreate: false,
        showEdit: false,
        showDelete: false,
    },
)

const emit = defineEmits<{
    create: []
    edit: [item: recordStringUnknown]
    delete: [item: recordStringUnknown]
    'update:selected': [value: recordStringUnknown[] | recordStringUnknown]
    'selection-change': [value: recordStringUnknown[] | recordStringUnknown]
}>()

const attrs: recordStringUnknown = useAttrs()

const internalSelectedItems = ref<recordStringUnknown[]>([])

const filters = reactive<Record<string, string>>({})

const allFiltersVisible = ref<boolean>(false)

const DEFAULT_STATE_ITEMS: { title: string; value: string }[] = [
    { title: t('active'), value: 'S' },
    { title: t('inactive'), value: 'N' },
]

function getHeaderKey(header: DataTableHeader): string {
    return header.key || header.value || ''
}

function getFilterType(header: DataTableHeader | TableHeader): FilterType | null {
    const f = (header as TableHeader).filterConfig ?? (header as DataTableHeader).filter
    if (f === false) return null
    if (typeof f === 'boolean' || f === undefined) return 'text'
    return (f as ColumnFilter).type ?? 'text'
}

function getFilterItems(header: DataTableHeader | TableHeader): unknown[] {
    const f = (header as TableHeader).filterConfig ?? (header as DataTableHeader).filter
    if (f && typeof f === 'object') {
        const items = (f as ColumnFilter).items
        if (items && items.length > 0) return items
    }

    if (getFilterType(header) === 'state') return DEFAULT_STATE_ITEMS
    return []
}

function toggleAllFilters(): void {
    allFiltersVisible.value = !allFiltersVisible.value
    clearAllFilters()
}

function clearAllFilters(): void {
    props.headers.forEach((h: DataTableHeader) => {
        filters[getHeaderKey(h)] = ''
    })
}

clearAllFilters()

const processedHeaders = computed<TableHeader[]>(() => {
    const base: TableHeader[] = props.headers.map((h: DataTableHeader) => ({
        title: h.title ?? h.text ?? '',
        key: getHeaderKey(h),
        filterConfig: h.filter,
        sortable: false,
        width: h.width,
        align: (h.align || 'center') as 'start' | 'end' | 'center',
    }))

    if (props.showEdit || props.showDelete) {
        base.push({
            title: t('actions'),
            key: 'actions',
            filterConfig: false,
            sortable: false,
            width: undefined,
            align: 'center',
        })
    }

    return base
})

const stateHeaders = computed<TableHeader[]>(() =>
    processedHeaders.value.filter((h) => getFilterType(h) === 'state'),
)

const actualItemKey = computed<string>(() => {
    if (props.itemKey) return props.itemKey
    return '__index'
})

const tableAttrs = computed<recordStringUnknown>(() => ({
    ...attrs,
    'items-per-page': 5,
    'items-per-page-options': [5, 10, 20, 50],
}))

const filteredItems = computed(() => {
    return props.items
        .map((item: recordStringUnknown, index: number) => ({ ...item, __index: index }))
        .filter((item: recordStringUnknown) => {
            const record: recordStringUnknown = item as recordStringUnknown

            return props.headers.every((header: DataTableHeader) => {
                if (header.filter === false) return true

                const key: string = getHeaderKey(header)
                const filterValue: string = filters[key]

                if (!filterValue) return true

                const value: unknown = record[key]

                if (value === undefined || value === null) return false

                switch (getFilterType(header)) {
                    case 'text':
                        return String(value).toLowerCase().includes(filterValue.toLowerCase())
                    case 'select':
                    case 'date':
                    case 'state':
                        return String(value).includes(String(filterValue))
                    default:
                        return true
                }
            })
        })
})

const selectedItems = computed<recordStringUnknown[]>({
    get: () => internalSelectedItems.value,

    set: (newValue: (recordStringUnknown | string | number)[]) => {
        const key: string = actualItemKey.value

        const converted: recordStringUnknown[] = newValue
            .map((val) =>
                typeof val === 'object'
                    ? val
                    : filteredItems.value.find((i: recordStringUnknown) => i[key] === val),
            )
            .filter((v): v is recordStringUnknown => Boolean(v))

        internalSelectedItems.value =
            props.selectionMode === 'single' && converted.length > 0
                ? [converted[converted.length - 1]]
                : converted

        const payload =
            props.selectionMode === 'single'
                ? internalSelectedItems.value[0]
                : internalSelectedItems.value

        emit('update:selected', payload)
        emit('selection-change', payload)
    },
})

function getRowProps({ item }: { item: recordStringUnknown }): { class?: string } {
    const key: string = actualItemKey.value

    const selected: boolean = selectedItems.value.some(
        (i: recordStringUnknown) => i[key] === item[key],
    )

    return selected ? { class: 'row-selected' } : {}
}

function handleRowClick(event: unknown, row: { item: recordStringUnknown }): unknown {
    const item: recordStringUnknown = row.item
    const key: string = actualItemKey.value

    if (props.selectionMode === 'none') return

    const index = selectedItems.value.findIndex((i: recordStringUnknown) => i[key] === item[key])

    if (props.selectionMode === 'single') {
        selectedItems.value = index > -1 ? [] : [item]
        return
    }

    if (props.selectionMode === 'multiple') {
        if (index > -1) internalSelectedItems.value.splice(index, 1)
        else internalSelectedItems.value.push(item)

        selectedItems.value = [...internalSelectedItems.value]
    }
}

function handleActionRowClick(type: 'edit' | 'delete', item: recordStringUnknown): void {
    if (type === 'edit') emit('edit', item)
    else if (type === 'delete') emit('delete', item)
    selectedItems.value = [item]
}
</script>

<style scoped>
.filter-container {
    width: 100%;
    padding-top: 2px;
}

.v-data-table.filters-active thead th {
    padding-bottom: 42px;
}

.row-selected {
    background-color: rgba(25, 118, 210, 0.15) !important;
}

:deep(thead th) {
    text-transform: uppercase;
    font-weight: 700 !important;
}

.v-data-table-header th {
    white-space: nowrap;
}

:deep(.v-data-table tbody tr td) {
    border-bottom: 1px solid rgba(0, 0, 0, 0.12) !important;
}

:deep(.v-data-table thead tr th) {
    border-bottom: 1px solid rgba(0, 0, 0, 0.12) !important;
}
</style>
