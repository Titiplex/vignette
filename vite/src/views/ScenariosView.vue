<script setup>
import {computed, onMounted, ref, watch} from "vue";
import {fetchScenarios, fetchScenarioThumbnails} from "../api/scenarios";
import ScenarioCard from "../components/ScenarioCard.vue";
import BasePageHeader from "../components/ui/BasePageHeader.vue";
import BaseLoader from "../components/ui/BaseLoader.vue";
import BaseAlert from "../components/ui/BaseAlert.vue";
import BaseEmptyState from "../components/ui/BaseEmptyState.vue";
import {useDebouncedRef} from "../composables/useDebouncedRef";
import {RouterLink} from "vue-router";

const scenarios = ref([]);
const previewMap = ref({});
const error = ref("");
const loading = ref(false);

const {source: search, debounced} = useDebouncedRef("", 250);
const effectiveSearch = ref("");

watch(debounced, (value) => {
  effectiveSearch.value = value.trim().toLowerCase();
});

const filtered = computed(() => {
  const q = effectiveSearch.value;
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
      <BasePageHeader
          title="Scenario gallery"
          subtitle="Browse published scenarios and open them to view thumbnails and audio recordings."
      >
        <template #actions>
          <RouterLink to="/create-scenario" class="btn btn--primary">
            Create scenario
          </RouterLink>
        </template>
      </BasePageHeader>

      <div class="card search-panel">
        <input v-model="search" placeholder="Search scenarios"/>
      </div>

      <BaseLoader v-if="loading">Loading scenarios...</BaseLoader>

      <BaseAlert v-else-if="error" type="error">
        {{ error }}
      </BaseAlert>

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

        <BaseEmptyState
            v-else
            title="No scenarios found"
            message="Try another search query."
        />
      </template>
    </section>
  </main>
</template>