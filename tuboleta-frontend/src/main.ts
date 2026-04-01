import { createPinia } from 'pinia'
import { createApp } from 'vue'
import App from './App.vue'
import { router } from './router'
import vuetify from './plugins/vuetify'
import i18n from '@/plugins/i18n'
import '@/scss/style.scss'
import { PerfectScrollbarPlugin } from 'vue3-perfect-scrollbar'
import 'vue3-perfect-scrollbar/style.css'
import swalPlugin from '@/plugins/swal'
import TableDynamic from '@/components/ui-components/table/TableDynamic.vue'
import Loading from '@/components/ui-components/loading/Loading.vue'
import FloatingCard from '@/components/ui-components/cards/FloatingCard.vue'

const app = createApp(App)

app.use(createPinia())
app.use(swalPlugin)
app.use(router)
app.use(PerfectScrollbarPlugin)
app.use(vuetify)
app.use(i18n)

app.component('VTableDynamic', TableDynamic)
app.component('VLoading', Loading)
app.component('VFloatingCard', FloatingCard)

app.mount('#app')
