import {ref} from "vue";

const toasts = ref([]);
let nextId = 1;

export function useToast() {
    function push(message, type = "info", timeout = 3000) {
        const id = nextId++;
        toasts.value.push({id, message, type});

        if (timeout > 0) {
            setTimeout(() => {
                remove(id);
            }, timeout);
        }
    }

    function success(message, timeout = 3000) {
        push(message, "success", timeout);
    }

    function error(message, timeout = 4000) {
        push(message, "error", timeout);
    }

    function info(message, timeout = 3000) {
        push(message, "info", timeout);
    }

    function remove(id) {
        toasts.value = toasts.value.filter((t) => t.id !== id);
    }

    return {
        toasts,
        push,
        success,
        error,
        info,
        remove,
    };
}