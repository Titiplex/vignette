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

const markers = computed(() => {
  return props.audios
      .filter((audio) =>
          audio?.markerX !== null &&
          audio?.markerX !== undefined &&
          audio?.markerY !== null &&
          audio?.markerY !== undefined &&
          audio?.markerX !== "" &&
          audio?.markerY !== ""
      )
      .map((audio) => ({
        ...audio,
        _x: Math.max(0, Math.min(100, Number(audio.markerX))),
        _y: Math.max(0, Math.min(100, Number(audio.markerY))),
      }))
      .filter((audio) => Number.isFinite(audio._x) && Number.isFinite(audio._y));
});

function markerStyle(marker) {
  return {
    left: `${marker._x}%`,
    top: `${marker._y}%`,
  };
}
</script>

<template>
  <article
      class="card thumb-card storyboard-tile"
      :class="{ selected, 'thumb-card--highlighted': highlighted }"
      :data-thumbnail-id="thumb.id"
      @click="emit('select', thumb)"
  >
    <div class="storyboard-tile__stage">
      <img
          :src="thumbnailContentUrl(thumb)"
          :alt="thumb.title || `Thumbnail ${thumb.id}`"
          class="storyboard-tile__image"
      />

      <button
          v-for="marker in markers"
          :key="marker.id"
          type="button"
          class="marker-dot storyboard-tile__marker"
          :style="markerStyle(marker)"
          :title="marker.markerLabel || marker.title || `Audio #${marker.id}`"
          @click.stop="emit('select', thumb)"
      >
        <span class="marker-dot__pulse"></span>
        <span class="marker-dot__core"></span>
      </button>
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
        <BaseBadge variant="warning">{{ markers.length }} marker</BaseBadge>
      </div>

      <p class="thumb-card__hint">
        Click to open its details, audio actions and layout settings.
      </p>
    </div>
  </article>
</template>