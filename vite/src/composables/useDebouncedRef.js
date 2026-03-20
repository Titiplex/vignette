import {ref, watch} from "vue";

export function useDebouncedRef(initialValue = "", delay = 300) {
    const source = ref(initialValue);
    const debounced = ref(initialValue);

    let timer = null;

    watch(source, (value) => {
        clearTimeout(timer);
        timer = setTimeout(() => {
            debounced.value = value;
        }, delay);
    });

    return {
        source,
        debounced,
    };
}