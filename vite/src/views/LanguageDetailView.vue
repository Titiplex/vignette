<script setup>
import {ref, watch} from "vue";
import {RouterLink} from "vue-router";
import {fetchLanguage, fetchLanguageScenarios} from "../api/languages";
import BasePageHeader from "../components/ui/BasePageHeader.vue";
import BaseLoader from "../components/ui/BaseLoader.vue";
import BaseAlert from "../components/ui/BaseAlert.vue";
import BaseEmptyState from "../components/ui/BaseEmptyState.vue";
import BaseBadge from "../components/ui/BaseBadge.vue";

const props = defineProps({
  id: {type: String, required: true},
});

const language = ref(null);
const scenarios = ref([]);
const error = ref("");
const loading = ref(false);

function levelVariant(level) {
  if (!level) return "neutral";
  const l = String(level).toLowerCase();
  if (l.includes("family")) return "info";
  if (l.includes("language")) return "success";
  if (l.includes("dialect")) return "warning";
  return "neutral";
}

async function load(id) {
  loading.value = true;
  error.value = "";
  language.value = null;
  scenarios.value = [];

  try {
    const [lang, linkedScenarios] = await Promise.all([
      fetchLanguage(id),
      fetchLanguageScenarios(id),
    ]);

    language.value = lang;
    scenarios.value = linkedScenarios;
  } catch (e) {
    error.value = e.message || "Failed to load language details.";
  } finally {
    loading.value = false;
  }
}

watch(
    () => props.id,
    (id) => {
      load(id);
    },
    {immediate: true}
);
</script>

<template>
  <main class="page">
    <BaseLoader v-if="loading">Loading language details...</BaseLoader>
    <BaseAlert v-else-if="error" type="error">{{ error }}</BaseAlert>

    <template v-else-if="language">
      <section class="section">
        <BasePageHeader
            :title="language.name ?? 'Language'"
            subtitle="Detailed language entry"
        >
          <template #actions>
            <BaseBadge :variant="levelVariant(language.level)">
              {{ language.level ?? "-" }}
            </BaseBadge>
          </template>
        </BasePageHeader>

        <div class="card info-grid info-grid--premium">
          <div>
            <h3>Family</h3>
            <p>
              <RouterLink v-if="language.familyId" :to="`/languages/${language.familyId}`">
                {{ language.familyName }}
              </RouterLink>
              <span v-else>-</span>
            </p>
          </div>

          <div>
            <h3>Parent</h3>
            <p>
              <RouterLink v-if="language.parentId" :to="`/languages/${language.parentId}`">
                {{ language.parentName }}
              </RouterLink>
              <span v-else>-</span>
            </p>
          </div>

          <div>
            <h3>Level</h3>
            <p>{{ language.level ?? "-" }}</p>
          </div>

          <div>
            <h3>ID</h3>
            <p class="table__mono">{{ language.id ?? "-" }}</p>
          </div>
        </div>

        <section class="card">
          <h2>Description</h2>
          <p class="text">{{ language.description ?? "No description available." }}</p>
        </section>

        <section class="section">
          <BasePageHeader
              title="Related scenarios"
              :subtitle="`${scenarios.length} scenario(s) linked to this language.`"
          />

          <div v-if="scenarios.length" class="table-wrap card table-card">
            <table class="table">
              <thead>
              <tr>
                <th>Name</th>
                <th>Author</th>
                <th>Created</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="s in scenarios" :key="s.id">
                <td>
                  <RouterLink :to="`/scenarios/${s.id}`" class="table__primary-link">
                    {{ s.title ?? "Untitled scenario" }}
                  </RouterLink>
                </td>
                <td>{{ s.authorUsername ?? "Unknown author" }}</td>
                <td>{{ s.createdAt ?? "-" }}</td>
              </tr>
              </tbody>
            </table>
          </div>

          <BaseEmptyState
              v-else
              title="No scenarios linked yet"
              message="This language is not yet associated with a scenario."
          />
        </section>
      </section>
    </template>
  </main>
</template>