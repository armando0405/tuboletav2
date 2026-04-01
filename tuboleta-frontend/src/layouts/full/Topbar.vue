<script setup lang="ts">
import { Icon } from '@iconify/vue'

const items = [
    {
        text: 'Templates',
        icon: 'window-frame-linear',
        url: 'https://adminmart.com/templates/vuejs/',
    },
    { text: 'Help', icon: 'question-circle-linear', url: 'https://adminmart.com/support/' },
    { text: 'Hire Us', icon: 'case-round-linear', url: 'https://adminmart.com/hire-us/' },
]
// Variable exported to template
void items
</script>
<template>
    <div class="feature-topbar w-full py-4 px-6 w-100">
        <div class="d-flex flex-lg-row flex-column gap-3 justify-space-between align-center">
            <div class="d-flex align-center ga-6">
                <a
                    href="https://adminmart.com/"
                    target="_blank"
                    class="lh-0"
                >
                    <img
                        src="@/assets/images/logos/logo-adminmart.svg"
                        width="148"
                    />
                </a>
                <div
                    class="d-lg-flex d-none items-center ga-4 topbar-links border-s border-opacity-25 ps-6"
                >
                    <a
                        v-for="(item, index) in items"
                        :key="index"
                        class="d-flex items-center p-0 ga-2 lh-normal"
                        variant="text"
                        :href="item.url"
                        target="_blank"
                    >
                        <Icon
                            :icon="'solar:' + item.icon"
                            height="20"
                            width="20"
                        />
                        {{ item.text }}
                    </a>
                </div>
            </div>
        </div>
    </div>
</template>
