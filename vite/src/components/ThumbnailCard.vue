<script setup>
import {buildApiUrl} from "../api/rest";
import BaseBadge from "./ui/BaseBadge.vue";

defineProps({
  thumb: {type: Object, required: true},
  audios: {type: Array, default: () => []},
  selected: {type: Boolean, default: false},
});

const emit = defineEmits(["select"]);

function thumbnailContentUrl(thumb) {
  if (!thumb?.id) return "";
  return buildApiUrl(`/api/thumbnails/${thumb.id}/content`);
}
</script>

<template>
  <article
      class="card thumb-card"
      :class="{ selected }"
      @click="emit('select', thumb)"
  >
    <img
        :src="thumbnailContentUrl(thumb)"
        :alt="thumb.title || `Thumbnail ${thumb.id}`"
        class="thumb-preview"
    />

    <div class="thumb-card__body">
      <div class="thumb-card__header">
        <h3 class="thumb-card__title">
          {{ thumb.title || `Thumbnail #${thumb.idx ?? thumb.id}` }}
        </h3>

        <BaseBadge :variant="selected ? 'success' : 'neutral'">
          {{ selected ? "Selected" : "Available" }}
        </BaseBadge>
      </div>

      <div class="thumb-card__meta-row">
        <BaseBadge variant="info">Index {{ thumb.idx ?? "-" }}</BaseBadge>
        <BaseBadge variant="neutral">{{ audios.length }} audio</BaseBadge>
      </div>

      <p class="thumb-card__hint">
        Click to manage audio and marker placement.
      </p>
    </div>
  </article>
</template>