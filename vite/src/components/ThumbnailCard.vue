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
      .map((audio) => {
        const x = Number(audio.markerX);
        const y = Number(audio.markerY);

        return {
          ...audio,
          _x: Number.isFinite(x) ? Math.max(0, Math.min(100, x)) : null,
          _y: Number.isFinite(y) ? Math.max(0, Math.min(100, y)) : null,
        };
      })
      .filter((audio) => audio._x !== null && audio._y !== null);
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

      <div class="storyboard-tile__overlay">
        <BaseBadge v-if="selected" variant="success">Selected</BaseBadge>
        <BaseBadge v-else variant="neutral">
          {{ thumb.idx ?? thumb.id }}
        </BaseBadge>
      </div>

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
  </article>
</template>