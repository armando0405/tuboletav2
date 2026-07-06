import { TuboletaDarkTheme } from '@/theme/DarkTheme'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export type NotifyType = 'error' | 'success' | 'info' | 'warning'

interface NotifyMessage {
    text: string
    color: string
}

const { error, success, info, warning } = TuboletaDarkTheme.colors

const colorMap: Record<NotifyType, string> = {
    error: error || '',
    success: success || '',
    info: info || '',
    warning: warning || '',
}

export const useNotifyStore = defineStore('notify', () => {
    const messages = ref<NotifyMessage[]>([])

    function notify(text: string, type: NotifyType) {
        messages.value.push({ text, color: colorMap[type] })
    }

    return { messages, notify }
})
