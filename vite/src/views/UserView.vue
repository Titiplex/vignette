<script setup>
import {computed, onMounted, ref} from "vue";
import {RouterLink} from "vue-router";
import {fetchMyProfile, updateMyProfile} from "../api/users";
import {fetchMyScenarios, fetchScenarios} from "../api/scenarios";
import {useAuth} from "../composables/useAuth";

const {currentUser, loadMe} = useAuth();

const profile = ref({
  displayName: "",
  institution: "",
  researchInterests: "",
  bio: "",
  academyAffiliations: "",
  profilePublic: false,
});

const roles = ref([]);
const affiliations = ref([]);
const myScenarios = ref([]);
const error = ref("");
const success = ref("");
const loading = ref(false);
const loadingWorks = ref(false);
computed(() => myScenarios.value.length);

async function loadProfile() {
  const data = await fetchMyProfile();

  profile.value = {
    displayName: data.displayName ?? "",
    institution: data.institution ?? "",
    researchInterests: data.researchInterests ?? "",
    bio: data.bio ?? "",
    academyAffiliations: (data.academyAffiliations ?? []).join("\n"),
    profilePublic: !!data.profilePublic,
  };

  roles.value = data.roles ?? [];
  affiliations.value = data.academyAffiliations ?? [];
}

async function loadMyWorks() {
  loadingWorks.value = true;
  try {
    await loadMe();
    const allScenarios = await fetchScenarios();
    const username = currentUser.value?.username ?? null;

    myScenarios.value = username
        ? allScenarios.filter((scenario) => scenario.authorUsername === username)
        : [];
  } finally {
    loadingWorks.value = false;
  }
}

async function save() {
  loading.value = true;
  error.value = "";
  success.value = "";

  try {
    await updateMyProfile({
      displayName: profile.value.displayName,
      institution: profile.value.institution,
      researchInterests: profile.value.researchInterests,
      bio: profile.value.bio,
      academyAffiliations: profile.value.academyAffiliations
          .split("\n")
          .map((s) => s.trim())
          .filter(Boolean),
      profilePublic: profile.value.profilePublic,
    });

    success.value = "Profile saved.";
    await loadProfile();
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}

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
    myScenarios.value = await fetchMyScenarios();
  } catch (e) {
    error.value = e.message || "Failed to load workspace.";
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  try {
    await Promise.all([
      loadProfile(),
      loadMyWorks(),
      loadWorkspace(),
    ]);
  } catch (e) {
    error.value = e.message;
  }
});
</script>

<template>
  <main class="page">
    <section class="section">
      <div class="section-heading">
        <div>
          <h1>User profile</h1>
          <p class="muted">Manage your public presence, affiliations and personal work.</p>
        </div>
      </div>

      <section class="form-card">
        <h2>Profile information</h2>

        <label>
          Display name
          <input v-model="profile.displayName"/>
        </label>

        <label>
          Institution
          <input v-model="profile.institution"/>
        </label>

        <label>
          Research interests
          <input v-model="profile.researchInterests"/>
        </label>

        <label>
          Biography
          <textarea v-model="profile.bio" rows="5"/>
        </label>

        <label>
          Academic affiliations (one per line)
          <textarea v-model="profile.academyAffiliations" rows="5"/>
        </label>

        <label class="checkbox-row">
          <input v-model="profile.profilePublic" type="checkbox"/>
          <span>Make my profile public</span>
        </label>

        <button class="btn btn--primary" @click="save" :disabled="loading">
          {{ loading ? "Saving..." : "Save profile" }}
        </button>

        <p v-if="success" class="success">{{ success }}</p>
        <p v-if="error" class="error">{{ error }}</p>
      </section>

      <div class="card-grid">
        <section class="card">
          <h3>Roles</h3>
          <ul v-if="roles.length" class="plain-list">
            <li v-for="r in roles" :key="r">{{ r }}</li>
          </ul>
          <p v-else class="muted">No roles available.</p>
        </section>

        <section class="card">
          <h3>Affiliations</h3>
          <ul v-if="affiliations.length" class="plain-list">
            <li v-for="a in affiliations" :key="a">{{ a }}</li>
          </ul>
          <p v-else class="muted">No affiliations listed.</p>
        </section>
      </div>
    </section>

    <div>
      <hr class="hline">
    </div>

    <section class="card section">
      <div class="section-heading">
        <div>
          <h2>Workspace</h2>
          <p class="muted">
            Private overview for your scenarios, publication state and management shortcuts.
          </p>
        </div>
      </div>

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

      <div v-if="loadingWorks" class="loader-block">
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
          You do not have any scenario yet.
        </p>
      </div>
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