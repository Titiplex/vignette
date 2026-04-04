<script setup>
import {computed, onMounted, ref} from "vue";
import {RouterLink} from "vue-router";
import {fetchAdminScenarios, updateAdminScenarioVisibility} from "../api/admin";

const loading = ref(false);
const savingScenarioId = ref(null);
const error = ref("");
const success = ref("");
const scenarios = ref([]);
const query = ref("");

const filteredScenarios = computed(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return scenarios.value;

  return scenarios.value.filter((scenario) => {
    return [
      scenario.title,
      scenario.description,
      scenario.authorUsername,
      scenario.languageId,
      scenario.visibilityStatus,
    ]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(q));
  });
});

async function loadScenarios() {
  loading.value = true;
  error.value = "";
  success.value = "";

  try {
    const rows = await fetchAdminScenarios();
    scenarios.value = rows.map((scenario) => ({
      ...scenario,
      draftVisibilityStatus: scenario.visibilityStatus ?? "DRAFT",
    }));
  } catch (e) {
    error.value = e.message || "Failed to load scenarios.";
  } finally {
    loading.value = false;
  }
}

function visibilityChanged(scenario) {
  return scenario.draftVisibilityStatus !== scenario.visibilityStatus;
}

async function saveVisibility(scenario) {
  savingScenarioId.value = scenario.id;
  error.value = "";
  success.value = "";

  try {
    const updated = await updateAdminScenarioVisibility(
        scenario.id,
        scenario.draftVisibilityStatus
    );

    const index = scenarios.value.findIndex((s) => s.id === scenario.id);
    if (index >= 0) {
      scenarios.value[index] = {
        ...updated,
        draftVisibilityStatus: updated.visibilityStatus,
      };
    }

    success.value = `Visibility updated for scenario #${updated.id}.`;
  } catch (e) {
    error.value = e.message || "Failed to update scenario visibility.";
  } finally {
    savingScenarioId.value = null;
  }
}

onMounted(loadScenarios);
</script>

<template>
  <main class="page">
    <section class="section">
      <div class="section-heading">
        <div>
          <h1>Admin scenarios</h1>
          <p class="muted">
            Review all scenarios and adjust their visibility status.
          </p>
        </div>

        <div class="toolbar">
          <input v-model="query" placeholder="Search scenarios..."/>
          <button class="btn btn--ghost" @click="loadScenarios">
            Refresh
          </button>
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>
      <p v-if="success" class="success">{{ success }}</p>

      <div v-if="loading" class="loader-block">
        <span class="loader-spinner"></span>
        <span class="muted">Loading scenarios...</span>
      </div>

      <div v-else-if="filteredScenarios.length" class="stack-list">
        <article v-for="scenario in filteredScenarios" :key="scenario.id" class="card">
          <div class="toolbar toolbar--spread toolbar--top">
            <div>
              <h2>{{ scenario.title || "Untitled scenario" }}</h2>
              <p class="muted">
                #{{ scenario.id }} · {{ scenario.authorUsername || "-" }} · {{ scenario.languageId || "-" }}
              </p>
            </div>

            <span class="badge">
              {{ scenario.visibilityStatus }}
            </span>
          </div>

          <p class="text">
            {{ scenario.description || "No description provided." }}
          </p>

          <div class="toolbar">
            <label>
              Visibility
              <select v-model="scenario.draftVisibilityStatus">
                <option value="DRAFT">DRAFT</option>
                <option value="PUBLISHED">PUBLISHED</option>
              </select>
            </label>

            <button
                class="btn btn--primary"
                :disabled="savingScenarioId === scenario.id || !visibilityChanged(scenario)"
                @click="saveVisibility(scenario)"
            >
              {{ savingScenarioId === scenario.id ? "Saving..." : "Save visibility" }}
            </button>

            <RouterLink :to="`/scenarios/${scenario.id}`" class="btn btn--ghost">
              Open
            </RouterLink>

            <RouterLink :to="`/scenarios/${scenario.id}/manage`" class="btn btn--ghost">
              Manage
            </RouterLink>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">
        <h3>No scenario found</h3>
        <p class="muted">Try another search term.</p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.stack-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar--spread {
  justify-content: space-between;
  align-items: center;
}

.toolbar--top {
  align-items: flex-start;
}

.badge {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 0.25rem 0.7rem;
  background: var(--surface-alt);
  font-size: 0.82rem;
}
</style>