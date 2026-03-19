<script setup>
const props = defineProps({
  thumb: {type: Object, required: true},
  audios: {type: Array, default: () => []},
  selected: {type: Boolean, default: false},
});

const emit = defineEmits(["select"]);
</script>

<template>
  <div class="card thumb-card" :class="{ selected }" @click="emit('select', thumb)">
    <img
        :src="`/api/thumbnails/${thumb.id}/content`"
        :alt="thumb.title || ''"
        class="thumb-preview"
    />

    <div class="caption">
      #{{ thumb.idx ?? "?" }} — {{ thumb.title || `thumb ${thumb.id}` }}
    </div>

    <div class="thumb-audio-list">
      <p v-if="!audios.length">No audio yet.</p>

      <div v-for="a in audios" :key="a.id" class="thumb-audio-item">
        <div class="thumb-audio-title">
          #{{ a.idx }} — {{ a.title || "Untitled audio" }}
        </div>
        <audio controls :src="`/api/audios/${a.id}/content`"/>
      </div>
    </div>
  </div>
</template>