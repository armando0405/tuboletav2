import { useNotifyStore, type NotifyType } from '@/stores/notify.store'

export function useNotify() {
    const { notify } = useNotifyStore()
    return { notify } as { notify: (text: string, type: NotifyType) => void }
}
