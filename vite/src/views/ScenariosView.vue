<script setup>
import {computed, onMounted, ref} from "vue";
import {fetchScenarios, fetchScenarioThumbnails} from "../api/scenarios";
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
    const language = String(s.languageId ?? "").toLowerCase();
    const author = (s.authorUsername ?? "").toLowerCase();
    const description = (s.description ?? "").toLowerCase();

    return (
        title.includes(q) ||
        language.includes(q) ||
        author.includes(q) ||
        description.includes(q)
    );
  });
});

async function load() {
  loading.value = true;
  error.value = "";

  try {
    const data = await fetchScenarios();
    scenarios.value = Array.isArray(data) ? data : [];

    const map = {};
    await Promise.all(
        scenarios.value.map(async (s) => {
          try {
            const thumbs = await fetchScenarioThumbnails(s.id);
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
    <section class="section">
      <div class="section-heading">
        <div>
          <h1>Scenario gallery</h1>
          <p class="muted">
            Browse published scenarios and open them to view thumbnails and audio recordings.
          </p>
        </div>
      </div>

      <div class="card search-panel">
        <input v-model="search" placeholder="Search scenarios"/>
      </div>

      <p v-if="loading" class="muted">Loading scenarios...</p>
      <p v-else-if="error" class="error">{{ error }}</p>
      <template v-else>
        <div class="results-meta">
          <span>{{ filtered.length }} scenario(s)</span>
        </div>

        <section v-if="filtered.length" class="card-grid">
          <ScenarioCard
              v-for="s in filtered"
              :key="s.id"
              :scenario="s"
              :preview-id="previewMap[s.id]"
          />
        </section>

        <div v-else class="card empty-state">
          <h3>No scenarios found</h3>
          <p class="muted">Try another search query.</p>
        </div>
      </template>
    </section>
  </main>
</template>