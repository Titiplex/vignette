<script setup>
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {fetchLanguageOptions} from "../api/languages";
import {createScenario} from "../api/scenarios";
import BasePageHeader from "../components/ui/BasePageHeader.vue";
import BaseAlert from "../components/ui/BaseAlert.vue";
import BaseBadge from "../components/ui/BaseBadge.vue";
import {useToast} from "../composables/useToast";

const router = useRouter();
const toast = useToast();

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
const loading = ref(false);

const titleError = computed(() => {
  if (!form.value.title.trim()) return "Title is required.";
  if (form.value.title.trim().length < 3) return "Title is too short.";
  return "";
});

const languageError = computed(() => {
  if (!form.value.languageId) return "Please choose a language.";
  return "";
});

const isFormValid = computed(() => {
  return !titleError.value && !languageError.value;
});

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
  if (!isFormValid.value) {
    error.value = titleError.value || languageError.value;
    return;
  }

  loading.value = true;
  error.value = "";

  try {
    const created = await createScenario({
      title: form.value.title.trim(),
      description: form.value.description,
      languageId: form.value.languageId,
    });

    toast.success("Scenario created successfully.");
    router.push(`/scenarios/${created.id}`);
  } catch (e) {
    error.value = e.message;
    toast.error(e.message || "Failed to create scenario.");
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
      <BasePageHeader
          title="Create a scenario"
          subtitle="Set up a vignette sequence. Newly created scenarios start as drafts and stay private until published."
      />

      <form @submit.prevent="submit" class="form-card form-card--premium">
        <div class="meta-badges">
          <BaseBadge variant="info">Research workflow</BaseBadge>
          <BaseBadge variant="neutral">Scenario builder</BaseBadge>
          <BaseBadge variant="warning">Starts as draft</BaseBadge>
        </div>

        <label>
          Title
          <input v-model="form.title" placeholder="Scenario title"/>
          <span v-if="form.title && titleError" class="field-error">
            {{ titleError }}
          </span>
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
          <span v-if="languageError" class="field-error">
            {{ languageError }}
          </span>
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

        <button type="submit" class="btn btn--primary" :disabled="loading || !isFormValid">
          {{ loading ? "Creating..." : "Create scenario" }}
        </button>

        <BaseAlert v-if="error" type="error">
          {{ error }}
        </BaseAlert>
      </form>
    </section>
  </main>
</template>