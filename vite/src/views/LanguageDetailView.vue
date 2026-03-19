<script setup>
import {onMounted, ref} from "vue";
import {RouterLink} from "vue-router";
import {fetchLanguage, fetchLanguageScenarios} from "../api/languages";

const props = defineProps({
  id: {type: String, required: true},
});

const language = ref(null);
const scenarios = ref([]);
const error = ref("");
const loading = ref(false);

async function load() {
  loading.value = true;
  error.value = "";

  try {
    language.value = await fetchLanguage(props.id);
    scenarios.value = await fetchLanguageScenarios(props.id);
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
    <p v-if="loading" class="muted">Loading language details...</p>
    <p v-else-if="error" class="error">{{ error }}</p>

    <template v-else-if="language">
      <section class="section">
        <div class="section-heading">
          <div>
            <h1>{{ language.name ?? "Language" }}</h1>
            <p class="muted">Detailed language entry</p>
          </div>
        </div>

        <div class="card info-grid">
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
            <p>{{ language.id ?? "-" }}</p>
          </div>
        </div>

        <section class="card">
          <h2>Description</h2>
          <p class="text">{{ language.description ?? "No description available." }}</p>
        </section>

        <section class="section">
          <div class="section-heading">
            <div>
              <h2>Related scenarios</h2>
              <p class="muted">{{ scenarios.length }} scenario(s) linked to this language.</p>
            </div>
          </div>

          <div v-if="scenarios.length" class="table-wrap card">
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
                  <RouterLink :to="`/scenarios/${s.id}`">
                    {{ s.title ?? "Untitled scenario" }}
                  </RouterLink>
                </td>
                <td>{{ s.authorUsername ?? "Unknown author" }}</td>
                <td>{{ s.createdAt ?? "-" }}</td>
              </tr>
              </tbody>
            </table>
          </div>

          <div v-else class="card empty-state">
            <h3>No scenarios linked yet</h3>
            <p class="muted">This language is not yet associated with a scenario.</p>
          </div>
        </section>
      </section>
    </template>
  </main>
</template>