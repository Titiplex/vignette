<script setup>
import {computed} from "vue";

const props = defineProps({
  scenario: {type: Object, required: true},
  previewId: {type: [String, Number, null], default: null},
});

const description = computed(() => {
  const raw = props.scenario.description?.trim() || "No description available.";
  if (raw.length <= 140) return raw;
  return raw.slice(0, 137) + "...";
});
</script>

<template>
  <article class="card scenario-card">
    <div class="scenario-card__media">
      <img
          v-if="previewId"
          :src="`/api/thumbnails/${previewId}/content`"
          :alt="scenario.title ?? 'Scenario preview'"
          class="scenario-card__image"
      />
      <div v-else class="scenario-card__placeholder">
        No preview image yet
      </div>
    </div>

    <div class="scenario-card__body">
      <h3 class="scenario-card__title">
        {{ scenario.title ?? "Untitled scenario" }}
      </h3>

      <p class="scenario-card__description">
        {{ description }}
      </p>

      <div class="scenario-card__meta">
        <span><strong>Language:</strong> {{ scenario.languageId ?? "-" }}</span>
        <span><strong>Author:</strong> {{ scenario.authorUsername ?? "-" }}</span>
      </div>

      <RouterLink :to="`/scenarios/${scenario.id}`" class="btn btn--primary">
        Open scenario
      </RouterLink>
    </div>
  </article>
</template>