<script setup>
import {onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {apiFetch} from "../api/rest";
import {useAuth} from "../composables/useAuth";

const router = useRouter();
const {isAuthenticated, loadMe} = useAuth();

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

async function guard() {
  await loadMe();
  if (!isAuthenticated.value) {
    router.push("/login");
  }
}

async function loadProfile() {
  const data = await apiFetch("/api/users/me");
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
  error.value = "";
  success.value = "";
  try {
    await apiFetch("/api/users/me", {
      method: "PUT",
      body: {
        displayName: profile.value.displayName,
        institution: profile.value.institution,
        researchInterests: profile.value.researchInterests,
        biography: profile.value.biography,
        affiliations: profile.value.affiliationsText
            .split("\n")
            .map((s) => s.trim())
            .filter(Boolean),
        publicProfile: profile.value.publicProfile,
      },
    });
    success.value = "Profile saved.";
    await loadProfile();
  } catch (e) {
    error.value = e.message;
  }
}

onMounted(async () => {
  try {
    await guard();
    await loadProfile();
  } catch (e) {
    error.value = e.message;
  }
});
</script>

<template>
  <main class="page">
    <h1>User profile</h1>
    <p>Manage your public presence and academic affiliations.</p>

    <section class="form-card">
      <h2>My profile</h2>

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
        Academy / university affiliations (one per line)
        <textarea v-model="profile.affiliationsText" rows="5"/>
      </label>

      <label>
        <input v-model="profile.publicProfile" type="checkbox"/>
        Make my profile public
      </label>

      <button @click="save">Save profile</button>

      <p v-if="success">{{ success }}</p>
      <p v-if="error" class="error">{{ error }}</p>
    </section>

    <section class="card">
      <h3>Roles</h3>
      <ul>
        <li v-for="r in roles" :key="r">{{ r }}</li>
      </ul>
    </section>

    <section class="card">
      <h3>Affiliations</h3>
      <ul>
        <li v-for="a in affiliations" :key="a">{{ a }}</li>
      </ul>
    </section>
  </main>
</template>