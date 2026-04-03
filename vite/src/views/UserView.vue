<script setup>
import {computed, onMounted, ref} from "vue";
import {RouterLink} from "vue-router";
import {fetchMyProfile, updateMyProfile} from "../api/users";
import {fetchScenarios} from "../api/scenarios";
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

const visibleScenarioCount = computed(() => myScenarios.value.length);

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

onMounted(async () => {
  try {
    await Promise.all([
      loadProfile(),
      loadMyWorks(),
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

      <section class="card">
        <div class="section-heading">
          <div>
            <h2>My work</h2>
            <p class="muted">
              Quick access to your visible scenarios.
            </p>
          </div>
          <RouterLink to="/create-scenario" class="btn btn--primary">
            New scenario
          </RouterLink>
        </div>

        <p class="muted">
          {{ visibleScenarioCount }} scenario(s) currently visible from your account.
        </p>

        <div v-if="loadingWorks" class="loader-block">
          <span class="loader-spinner"></span>
          <span class="muted">Loading your work...</span>
        </div>

        <div v-else-if="myScenarios.length" class="card-grid">
          <article v-for="scenario in myScenarios" :key="scenario.id" class="card">
            <h3>{{ scenario.title || "Untitled scenario" }}</h3>
            <p class="text">
              {{ scenario.description || "No description provided." }}
            </p>
            <p class="muted">
              Created: {{ scenario.createdAt || "-" }}
            </p>
            <RouterLink :to="`/scenarios/${scenario.id}`">
              Open scenario
            </RouterLink>
          </article>
        </div>

        <div v-else class="empty-state">
          <h3>No visible scenario yet</h3>
          <p class="muted">
            You have not published or created any visible scenario yet.
          </p>
        </div>
      </section>
    </section>
  </main>
</template>