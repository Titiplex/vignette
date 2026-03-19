<script setup>
const props = defineProps({
  thumb: {type: Object, required: true},
  audios: {type: Array, default: () => []},
  selected: {type: Boolean, default: false},
});

const emit = defineEmits(["select"]);
</script>

<template>
  <article
      class="card thumb-card"
      :class="{ selected }"
      @click="emit('select', thumb)"
  >
    <img
        :src="`/api/thumbnails/${thumb.id}/content`"
        :alt="thumb.title || `Thumbnail ${thumb.id}`"
        class="thumb-preview"
    />

    <div class="thumb-card__body">
      <h3 class="thumb-card__title">
        {{ thumb.title || `Thumbnail #${thumb.idx ?? thumb.id}` }}
      </h3>

      <p class="thumb-card__meta">
        <strong>Index:</strong> {{ thumb.idx ?? "-" }}
      </p>

      <p class="thumb-card__meta">
        <strong>Audio clips:</strong> {{ audios.length }}
      </p>

      <p class="thumb-card__hint">
        Click to manage audio and marker placement.
      </p>
    </div>
  </article>
</template>