<script setup>
import {computed, onMounted, ref} from "vue";
import {apiFetch} from "../api/rest";
import ScenarioCard from "../components/ScenarioCard.vue";

const scenarios = ref([]);
const previewMap = ref({});
const search = ref("");
const error = ref("");
const loading = ref(false);

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase();
  if (!q) return scenarios.value;

  return scenarios.value.filter((s) => {
    const title = (s.title ?? "").toLowerCase();
    const language = (s.languageId ?? "").toLowerCase();
    const author = (s.authorUsername ?? "").toLowerCase();
    return title.includes(q) || language.includes(q) || author.includes(q);
  });
});

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const data = await apiFetch("/api/scenarios");
    scenarios.value = Array.isArray(data) ? data : [];

    const map = {};
    await Promise.all(
        scenarios.value.map(async (s) => {
          try {
            const thumbs = await apiFetch(`/api/scenarios/${s.id}/thumbnails`);
            map[s.id] = thumbs?.[0]?.id ?? null;
          } catch {
            map[s.id] = null;
          }
        })
    );
    previewMap.value = map;
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <main class="page">
    <h1>Open Scenario Gallery</h1>
    <p>Browse all published scenarios and open one to view its thumbnails and audio recordings.</p>

    <input v-model="search" placeholder="Search scenarios"/>

    <p v-if="loading">Loading...</p>
    <p v-else-if="error">{{ error }}</p>
    <p v-else>{{ filtered.length }} scenario(s) available</p>

    <section class="card-grid">
      <ScenarioCard
          v-for="s in filtered"
          :key="s.id"
          :scenario="s"
          :preview-id="previewMap[s.id]"
      />
    </section>
  </main>
</template>