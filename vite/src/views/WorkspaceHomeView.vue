<script setup>
import {computed, onMounted, ref} from "vue";
import {RouterLink} from "vue-router";
import {fetchScenarios} from "../api/scenarios";
import {useAuth} from "../composables/useAuth";

const {currentUser, loadMe, isAdmin} = useAuth();

const loading = ref(false);
const error = ref("");
const myScenarios = ref([]);

const privateCount = computed(() =>
    myScenarios.value.filter((s) => s.visibilityStatus !== "PUBLISHED").length
);

const publishedCount = computed(() =>
    myScenarios.value.filter((s) => s.visibilityStatus === "PUBLISHED").length
);

async function loadWorkspace() {
  loading.value = true;
  error.value = "";

  try {
    await loadMe();
    const scenarios = await fetchScenarios();
    const username = currentUser.value?.username ?? null;

    myScenarios.value = username
        ? scenarios.filter((scenario) => scenario.authorUsername === username)
        : [];
  } catch (e) {
    error.value = e.message || "Failed to load workspace.";
  } finally {
    loading.value = false;
  }
}

onMounted(loadWorkspace);
</script>

<template>
  <main class="page">
    <section class="section">
      <div class="section-heading">
        <div>
          <h1>Workspace</h1>
          <p class="muted">
            Private overview for your scenarios, publication state and management shortcuts.
          </p>
        </div>

        <div class="toolbar">
          <RouterLink to="/create-scenario" class="btn btn--primary">
            New scenario
          </RouterLink>
          <RouterLink v-if="isAdmin" to="/admin" class="btn btn--ghost">
            Admin dashboard
          </RouterLink>
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <div class="card-grid">
        <section class="card">
          <h3>My scenarios</h3>
          <p class="text">{{ myScenarios.length }}</p>
        </section>

        <section class="card">
          <h3>Published</h3>
          <p class="text">{{ publishedCount }}</p>
        </section>

        <section class="card">
          <h3>Private / draft</h3>
          <p class="text">{{ privateCount }}</p>
        </section>
      </div>

      <section class="card">
        <div class="section-heading">
          <div>
            <h2>Scenario management</h2>
            <p class="muted">
              Open the private management interface for storyboard governance and publication.
            </p>
          </div>
        </div>

        <div v-if="loading" class="loader-block">
          <span class="loader-spinner"></span>
          <span class="muted">Loading workspace...</span>
        </div>

        <div v-else-if="myScenarios.length" class="card-grid">
          <article v-for="scenario in myScenarios" :key="scenario.id" class="card">
            <div class="toolbar toolbar--spread">
              <h3>{{ scenario.title || "Untitled scenario" }}</h3>
              <span class="badge">{{ scenario.visibilityStatus || "UNKNOWN" }}</span>
            </div>

            <p class="text">
              {{ scenario.description || "No description provided." }}
            </p>

            <p class="muted">
              Language: {{ scenario.languageId || "-" }}
            </p>

            <div class="toolbar">
              <RouterLink :to="`/scenarios/${scenario.id}`" class="btn btn--ghost">
                Open
              </RouterLink>
              <RouterLink :to="`/scenarios/${scenario.id}/manage`" class="btn btn--primary">
                Manage
              </RouterLink>
            </div>
          </article>
        </div>

        <div v-else class="empty-state">
          <h3>No scenario available</h3>
          <p class="muted">
            You do not have any visible scenario in this list yet.
          </p>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 0.2rem 0.65rem;
  font-size: 0.82rem;
  background: var(--surface-alt);
}

.toolbar--spread {
  justify-content: space-between;
  align-items: center;
}
</style>