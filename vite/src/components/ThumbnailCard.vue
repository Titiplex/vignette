<script setup>
import {computed} from "vue";
import {buildApiUrl} from "../api/rest";
import BaseBadge from "./ui/BaseBadge.vue";

const props = defineProps({
  thumb: {type: Object, required: true},
  audios: {type: Array, default: () => []},
  selected: {type: Boolean, default: false},
  highlighted: {type: Boolean, default: false},
});

const emit = defineEmits(["select"]);

function thumbnailContentUrl(thumb) {
  if (!thumb?.id) return "";
  return buildApiUrl(`/api/thumbnails/${thumb.id}/content`);
}

const markerCount = computed(() => {
  return props.audios.filter((audio) =>
      audio?.markerX !== null &&
      audio?.markerX !== undefined &&
      audio?.markerY !== null &&
      audio?.markerY !== undefined &&
      audio?.markerX !== "" &&
      audio?.markerY !== ""
  ).length;
});
</script>

<template>
  <article
      class="card thumb-card"
      :class="{ selected, 'thumb-card--highlighted': highlighted }"
      :data-thumbnail-id="thumb.id"
      @click="emit('select', thumb)"
  >
    <div class="thumb-card__media">
      <div class="thumb-preview-frame">
        <img
            :src="thumbnailContentUrl(thumb)"
            :alt="thumb.title || `Thumbnail ${thumb.id}`"
            class="thumb-preview"
        />
      </div>
    </div>

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
        <BaseBadge variant="warning">{{ markerCount }} marker</BaseBadge>
      </div>

      <p class="thumb-card__hint">
        Click to manage audio and marker placement.
      </p>
    </div>
  </article>
</template>