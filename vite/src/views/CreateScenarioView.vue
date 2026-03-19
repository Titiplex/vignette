<script setup>
import {onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {apiFetch} from "../api/rest";
import {useAuth} from "../composables/useAuth";

const router = useRouter();
const {isAuthenticated, loadMe} = useAuth();

const form = ref({
  title: "",
  description: "",
  languageId: "",
});

const languages = ref([]);
const filter = ref("");
const page = ref(0);
const totalPages = ref(0);
const error = ref("");

async function guard() {
  await loadMe();
  if (!isAuthenticated.value) {
    router.push("/login");
  }
}

async function loadLanguages(pageNumber = 0, q = "") {
  const params = new URLSearchParams({
    page: String(pageNumber),
    size: "50",
  });

  if (q.trim()) {
    params.set("q", q.trim());
  }

  const data = await apiFetch(`/api/languages/options?${params.toString()}`);
  languages.value = data.content ?? [];
  page.value = data.number ?? 0;
  totalPages.value = data.totalPages ?? 0;
}

async function submit() {
  error.value = "";
  try {
    const created = await apiFetch("/api/scenarios", {
      method: "POST",
      body: {
        title: form.value.title.trim(),
        description: form.value.description,
        languageId: form.value.languageId,
      },
    });

    router.push(`/scenarios/${created.id}`);
  } catch (e) {
    error.value = e.message;
  }
}

async function refreshLanguages() {
  try {
    await loadLanguages(0, filter.value);
  } catch (e) {
    error.value = e.message;
  }
}

async function prevPage() {
  if (page.value <= 0) return;
  await loadLanguages(page.value - 1, filter.value);
}

async function nextPage() {
  if (page.value >= totalPages.value - 1) return;
  await loadLanguages(page.value + 1, filter.value);
}

onMounted(async () => {
  try {
    await guard();
    await loadLanguages();
  } catch (e) {
    error.value = e.message;
  }
});
</script>

<template>
  <main class="page">
    <h1>Create a Scenario</h1>
    <p>Set up a vignette sequence to collect oral responses in context.</p>

    <form @submit.prevent="submit" class="form-card">
      <label>
        Title
        <input v-model="form.title"/>
      </label>

      <label>
        Description
        <textarea v-model="form.description" rows="4"/>
      </label>

      <label>
        Language filter
        <input v-model="filter" @input="refreshLanguages"/>
      </label>

      <label>
        Language
        <select v-model="form.languageId">
          <option value="">-- choose --</option>
          <option v-for="l in languages" :key="l.id" :value="l.id">
            {{ l.name }}
          </option>
        </select>
      </label>

      <div class="toolbar">
        <button type="button" @click="prevPage" :disabled="page <= 0">Previous</button>
        <span>{{ totalPages > 0 ? `${page + 1}/${totalPages}` : "No result" }}</span>
        <button type="button" @click="nextPage" :disabled="page >= totalPages - 1">
          Next
        </button>
      </div>

      <button type="submit">Create scenario</button>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </main>
</template>