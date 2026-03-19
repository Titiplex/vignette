<script setup>
import {onMounted, ref} from "vue";
import {fetchMyProfile, updateMyProfile} from "../api/users";

const profile = ref({
  displayName: "",
  institution: "",
  researchInterests: "",
  biography: "",
  affiliationsText: "",
  publicProfile: false,
});

const roles = ref([]);
const affiliations = ref([]);
const error = ref("");
const success = ref("");
const loading = ref(false);

async function loadProfile() {
  const data = await fetchMyProfile();

  profile.value = {
    displayName: data.displayName ?? "",
    institution: data.institution ?? "",
    researchInterests: data.researchInterests ?? "",
    biography: data.biography ?? "",
    affiliationsText: (data.affiliations ?? []).join("\n"),
    publicProfile: !!data.publicProfile,
  };

  roles.value = data.roles ?? [];
  affiliations.value = data.affiliations ?? [];
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
      biography: profile.value.biography,
      affiliations: profile.value.affiliationsText
          .split("\n")
          .map((s) => s.trim())
          .filter(Boolean),
      publicProfile: profile.value.publicProfile,
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
    await loadProfile();
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
          <p class="muted">Manage your public presence and academic affiliations.</p>
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
          <textarea v-model="profile.biography" rows="5"/>
        </label>

        <label>
          Academic affiliations (one per line)
          <textarea v-model="profile.affiliationsText" rows="5"/>
        </label>

        <label class="checkbox-row">
          <input v-model="profile.publicProfile" type="checkbox"/>
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
  </main>
</template>