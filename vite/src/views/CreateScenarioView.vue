<script setup>
import {onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {fetchLanguageOptions} from "../api/languages";
import {createScenario} from "../api/scenarios";

const router = useRouter();

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
const success = ref("");
const loading = ref(false);

async function loadLanguages(pageNumber = 0, q = "") {
  const params = new URLSearchParams({
    page: String(pageNumber),
    size: "50",
  });

  if (q.trim()) {
    params.set("q", q.trim());
  }

  const data = await fetchLanguageOptions(params);
  languages.value = data.content ?? [];
  page.value = data.number ?? 0;
  totalPages.value = data.totalPages ?? 0;
}

async function submit() {
  loading.value = true;
  error.value = "";
  success.value = "";

  try {
    const created = await createScenario({
      title: form.value.title.trim(),
      description: form.value.description,
      languageId: form.value.languageId,
    });

    success.value = "Scenario created successfully.";
    router.push(`/scenarios/${created.id}`);
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
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
    await loadLanguages();
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
          <h1>Create a scenario</h1>
          <p class="muted">
            Set up a vignette sequence to collect oral responses in context.
          </p>
        </div>
      </div>

      <form @submit.prevent="submit" class="form-card">
        <label>
          Title
          <input v-model="form.title" placeholder="Scenario title"/>
        </label>

        <label>
          Description
          <textarea
              v-model="form.description"
              rows="5"
              placeholder="Describe the intended scenario and elicitation context"
          />
        </label>

        <label>
          Language filter
          <input
              v-model="filter"
              placeholder="Filter languages"
              @input="refreshLanguages"
          />
        </label>

        <label>
          Language
          <select v-model="form.languageId">
            <option value="">-- choose a language --</option>
            <option v-for="l in languages" :key="l.id" :value="l.id">
              {{ l.name }}
            </option>
          </select>
        </label>

        <div class="toolbar">
          <button type="button" class="btn btn--ghost" @click="prevPage" :disabled="page <= 0">
            Previous
          </button>

          <span class="muted">
            {{ totalPages > 0 ? `${page + 1} / ${totalPages}` : "No result" }}
          </span>

          <button
              type="button"
              class="btn btn--ghost"
              @click="nextPage"
              :disabled="page >= totalPages - 1"
          >
            Next
          </button>
        </div>

        <button type="submit" class="btn btn--primary" :disabled="loading">
          {{ loading ? "Creating..." : "Create scenario" }}
        </button>

        <p v-if="success" class="success">{{ success }}</p>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </section>
  </main>
</template>