<script setup>
import {onMounted, ref} from "vue";
import {RouterLink} from "vue-router";
import {fetchAdminOverview, fetchAdminScenarios, fetchAdminUsers} from "../api/admin";

const loading = ref(false);
const error = ref("");
const overview = ref(null);
const users = ref([]);
const scenarios = ref([]);

async function loadAdminDashboard() {
  loading.value = true;
  error.value = "";

  try {
    const [overviewData, userData, scenarioData] = await Promise.all([
      fetchAdminOverview(),
      fetchAdminUsers(),
      fetchAdminScenarios(),
    ]);

    overview.value = overviewData;
    users.value = userData;
    scenarios.value = scenarioData;
  } catch (e) {
    error.value = e.message || "Failed to load admin dashboard.";
  } finally {
    loading.value = false;
  }
}

onMounted(loadAdminDashboard);
</script>

<template>
  <main class="page">
    <section class="section">
      <div class="section-heading">
        <div>
          <h1>Admin dashboard</h1>
          <p class="muted">
            Global administration overview and moderation entry points.
          </p>
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <div v-if="loading" class="loader-block">
        <span class="loader-spinner"></span>
        <span class="muted">Loading admin dashboard...</span>
      </div>

      <template v-else>
        <div class="card-grid" v-if="overview">
          <section class="card">
            <h2>Users</h2>
            <p class="text">{{ overview.userCount }}</p>
          </section>

          <section class="card">
            <h2>Scenarios</h2>
            <p class="text">{{ overview.scenarioCount }}</p>
          </section>

          <section class="card">
            <h2>Published scenarios</h2>
            <p class="text">{{ overview.publishedScenarioCount }}</p>
          </section>

          <section class="card">
            <h2>Draft scenarios</h2>
            <p class="text">{{ overview.draftScenarioCount }}</p>
          </section>
        </div>

        <div class="card-grid">
          <section class="card">
            <h2>Users administration</h2>
            <p class="text">
              Review user accounts and update roles.
            </p>
            <p class="muted">Loaded users: {{ users.length }}</p>
            <RouterLink to="/admin/users" class="btn btn--primary">
              Open users admin
            </RouterLink>
          </section>

          <section class="card">
            <h2>Scenario administration</h2>
            <p class="text">
              Review all scenarios and moderate visibility.
            </p>
            <p class="muted">Loaded scenarios: {{ scenarios.length }}</p>
            <RouterLink to="/admin/scenarios" class="btn btn--primary">
              Open scenarios admin
            </RouterLink>
          </section>

          <section class="card">
            <h2>Community administration</h2>
            <p class="text">
              Moderate global accreditation requests and grants.
            </p>
            <RouterLink to="/admin/community" class="btn btn--primary">
              Open community admin
            </RouterLink>
          </section>

          <section class="card">
            <h2>Languages administration</h2>
            <p class="text">
              Search and update language entries.
            </p>
            <RouterLink to="/admin/languages" class="btn btn--primary">
              Open language admin
            </RouterLink>
          </section>
        </div>
      </template>
    </section>
  </main>
</template>