<script setup>
const props = defineProps({
  page: {type: Number, required: true},
  totalPages: {type: Number, required: true},
});

const emit = defineEmits(["go"]);

function pageWindow(current, total, windowSize = 2) {
  const start = Math.max(0, current - windowSize);
  const end = Math.min(total - 1, current + windowSize);
  return {start, end};
}
</script>

<template>
  <div class="pager" v-if="totalPages > 0">
    <button :disabled="page <= 0" @click="emit('go', 0)">« First</button>
    <button :disabled="page <= 0" @click="emit('go', page - 1)">‹ Prev</button>

    <template
        v-for="p in Array.from({ length: pageWindow(page, totalPages).end - pageWindow(page, totalPages).start + 1 }, (_, i) => i + pageWindow(page, totalPages).start)"
        :key="p">
      <button v-if="p !== page" @click="emit('go', p)">
        {{ p + 1 }}
      </button>
      <span v-else class="current-page">{{ p + 1 }}</span>
    </template>

    <button :disabled="page >= totalPages - 1" @click="emit('go', page + 1)">
      Next ›
    </button>
    <button :disabled="page >= totalPages - 1" @click="emit('go', totalPages - 1)">
      Last »
    </button>
  </div>
</template>