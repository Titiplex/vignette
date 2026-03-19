<script setup>
const props = defineProps({
  page: {type: Number, required: true},
  totalPages: {type: Number, required: true},
});

const emit = defineEmits(["go"]);

function visiblePages(current, total, radius = 2) {
  const start = Math.max(0, current - radius);
  const end = Math.min(total - 1, current + radius);
  return Array.from({length: end - start + 1}, (_, i) => start + i);
}
</script>

<template>
  <div v-if="totalPages > 1" class="pager">
    <button class="btn btn--ghost" :disabled="page <= 0" @click="emit('go', 0)">
      First
    </button>

    <button class="btn btn--ghost" :disabled="page <= 0" @click="emit('go', page - 1)">
      Prev
    </button>

    <button
        v-for="p in visiblePages(page, totalPages)"
        :key="p"
        class="pager__page"
        :class="{ 'is-current': p === page }"
        :disabled="p === page"
        @click="emit('go', p)"
    >
      {{ p + 1 }}
    </button>

    <button
        class="btn btn--ghost"
        :disabled="page >= totalPages - 1"
        @click="emit('go', page + 1)"
    >
      Next
    </button>

    <button
        class="btn btn--ghost"
        :disabled="page >= totalPages - 1"
        @click="emit('go', totalPages - 1)"
    >
      Last
    </button>
  </div>
</template>