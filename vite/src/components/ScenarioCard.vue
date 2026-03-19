<script setup>
const props = defineProps({
  scenario: {type: Object, required: true},
  previewId: {type: [String, Number, null], default: null},
});
</script>

<template>
  <article class="card thumb-card">
    <img
        v-if="previewId"
        :src="`/api/thumbnails/${previewId}/content`"
        :alt="scenario.title ?? 'Scenario preview'"
        class="thumb-preview"
    />
    <p v-else>No image preview yet.</p>

    <h3>{{ scenario.title ?? "Untitled scenario" }}</h3>
    <p>{{ scenario.description ?? "No description" }}</p>
    <p><strong>Language:</strong> {{ scenario.languageId ?? "-" }}</p>
    <p><strong>Author:</strong> {{ scenario.authorUsername ?? "-" }}</p>

    <RouterLink :to="`/scenarios/${scenario.id}`">Open scenario</RouterLink>
  </article>
</template>