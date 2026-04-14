<script setup>
import {computed, ref, watch} from "vue";
import {RouterLink} from "vue-router";
import {fetchLanguage, fetchLanguageScenarios, fetchMyLanguagePermissions, updateLanguage} from "../api/languages";
import {useAuth} from "../composables/useAuth";
import {useToast} from "../composables/useToast";
import BasePageHeader from "../components/ui/BasePageHeader.vue";
import BaseLoader from "../components/ui/BaseLoader.vue";
import BaseAlert from "../components/ui/BaseAlert.vue";
import BaseEmptyState from "../components/ui/BaseEmptyState.vue";
import BaseBadge from "../components/ui/BaseBadge.vue";

const props = defineProps({
  id: {type: String, required: true},
});

const {loadMe, isAuthenticated} = useAuth();
const toast = useToast();

const language = ref(null);
const scenarios = ref([]);
const permissions = ref({canEdit: false});
const error = ref("");
const loading = ref(false);
const saving = ref(false);
const saveError = ref("");
const saveSuccess = ref("");

const editForm = ref({
  name: "",
  level: "",
  bookkeeping: false,
  iso639P3code: "",
  latitude: "",
  longitude: "",
  countryIds: "",
  familyId: "",
  parentId: "",
  description: "",
  markupDescription: "",
});

const isEditing = ref(false);

function levelVariant(level) {
  if (!level) return "neutral";
  const l = String(level).toLowerCase();
  if (l.includes("family")) return "info";
  if (l.includes("language")) return "success";
  if (l.includes("dialect")) return "warning";
  return "neutral";
}

function hydrateForm(lang) {
  editForm.value = {
    name: lang?.name ?? "",
    level: lang?.level ?? "",
    bookkeeping: !!lang?.bookkeeping,
    iso639P3code: lang?.iso639P3code ?? "",
    latitude: lang?.latitude ?? "",
    longitude: lang?.longitude ?? "",
    countryIds: lang?.countryIds ?? "",
    familyId: lang?.familyId ?? "",
    parentId: lang?.parentId ?? "",
    description: lang?.description ?? "",
    markupDescription: lang?.markupDescription ?? "",
  };
}

const canEditLanguage = computed(() => !!permissions.value?.canEdit);

async function load(id) {
  loading.value = true;
  error.value = "";
  saveError.value = "";
  saveSuccess.value = "";
  language.value = null;
  scenarios.value = [];
  permissions.value = {canEdit: false};

  try {
    await loadMe();

    const baseCalls = [
      fetchLanguage(id),
      fetchLanguageScenarios(id),
    ];

    const canCheckPermissions = isAuthenticated.value;
    if (canCheckPermissions) {
      baseCalls.push(fetchMyLanguagePermissions(id));
    }

    const results = await Promise.all(baseCalls);

    language.value = results[0];
    scenarios.value = results[1];
    permissions.value = canCheckPermissions ? results[2] : {canEdit: false};

    hydrateForm(language.value);
  } catch (e) {
    error.value = e.message || "Failed to load language details.";
  } finally {
    loading.value = false;
  }
}

async function saveLanguage() {
  if (!language.value) return;

  saving.value = true;
  saveError.value = "";
  saveSuccess.value = "";

  try {
    const payload = {
      name: editForm.value.name,
      level: editForm.value.level,
      bookkeeping: editForm.value.bookkeeping,
      iso639P3code: editForm.value.iso639P3code,
      latitude: editForm.value.latitude === "" ? null : Number(editForm.value.latitude),
      longitude: editForm.value.longitude === "" ? null : Number(editForm.value.longitude),
      countryIds: editForm.value.countryIds,
      familyId: editForm.value.familyId,
      parentId: editForm.value.parentId,
      description: editForm.value.description,
      markupDescription: editForm.value.markupDescription,
    };

    language.value = await updateLanguage(language.value.id, payload);
    hydrateForm(language.value);
    isEditing.value = false;
    saveSuccess.value = "Language updated successfully.";
    toast.success(saveSuccess.value);
  } catch (e) {
    saveError.value = e.message || "Failed to update language.";
    toast.error(saveError.value);
  } finally {
    saving.value = false;
  }
}

function cancelEdit() {
  hydrateForm(language.value);
  isEditing.value = false;
  saveError.value = "";
  saveSuccess.value = "";
}

watch(
    () => props.id,
    (id) => {
      load(id);
    },
    {immediate: true}
);
</script>

<template>
  <main class="page">
    <BaseLoader v-if="loading">Loading language details...</BaseLoader>
    <BaseAlert v-else-if="error" type="error">{{ error }}</BaseAlert>

    <template v-else-if="language">
      <section class="section">
        <BasePageHeader
            :title="language.name ?? 'Language'"
            subtitle="Detailed language entry"
        >
          <template #actions>
            <BaseBadge :variant="levelVariant(language.level)">
              {{ language.level ?? "-" }}
            </BaseBadge>

            <BaseBadge v-if="canEditLanguage" variant="success">
              Can edit
            </BaseBadge>
          </template>
        </BasePageHeader>

        <div class="card info-grid info-grid--premium">
          <div>
            <h3>Family</h3>
            <p>
              <RouterLink v-if="language.familyId" :to="`/languages/${language.familyId}`">
                {{ language.familyName }}
              </RouterLink>
              <span v-else>-</span>
            </p>
          </div>

          <div>
            <h3>Parent</h3>
            <p>
              <RouterLink v-if="language.parentId" :to="`/languages/${language.parentId}`">
                {{ language.parentName }}
              </RouterLink>
              <span v-else>-</span>
            </p>
          </div>

          <div>
            <h3>Level</h3>
            <p>{{ language.level ?? "-" }}</p>
          </div>

          <div>
            <h3>ID</h3>
            <p class="table__mono">{{ language.id ?? "-" }}</p>
          </div>
        </div>

        <section class="card">
          <div class="section-heading">
            <div>
              <h2>Description</h2>
              <p class="muted">Language metadata and descriptive information.</p>
            </div>

            <button
                v-if="canEditLanguage && !isEditing"
                type="button"
                class="btn btn--primary"
                @click="isEditing = true"
            >
              Edit language
            </button>
          </div>

          <template v-if="isEditing">
            <div class="form-grid">
              <label>
                Name
                <input v-model="editForm.name"/>
              </label>

              <label>
                Level
                <input v-model="editForm.level"/>
              </label>

              <label>
                ISO 639-3
                <input v-model="editForm.iso639P3code"/>
              </label>

              <label>
                Country IDs
                <input v-model="editForm.countryIds"/>
              </label>

              <label>
                Family ID
                <input v-model="editForm.familyId"/>
              </label>

              <label>
                Parent ID
                <input v-model="editForm.parentId"/>
              </label>

              <label>
                Latitude
                <input v-model="editForm.latitude" type="number" step="any"/>
              </label>

              <label>
                Longitude
                <input v-model="editForm.longitude" type="number" step="any"/>
              </label>

              <label class="checkbox-row">
                <input v-model="editForm.bookkeeping" type="checkbox"/>
                <span>Bookkeeping</span>
              </label>

              <label class="form-grid__full">
                Description
                <textarea v-model="editForm.description" rows="6"/>
              </label>

              <label class="form-grid__full">
                Markup description
                <textarea v-model="editForm.markupDescription" rows="6"/>
              </label>
            </div>

            <div class="toolbar">
              <button class="btn btn--primary" :disabled="saving" @click="saveLanguage">
                {{ saving ? "Saving..." : "Save changes" }}
              </button>
              <button class="btn btn--ghost" :disabled="saving" @click="cancelEdit">
                Cancel
              </button>
            </div>

            <BaseAlert v-if="saveSuccess" type="success">{{ saveSuccess }}</BaseAlert>
            <BaseAlert v-if="saveError" type="error">{{ saveError }}</BaseAlert>
          </template>

          <template v-else>
            <p class="text">{{ language.description ?? "No description available." }}</p>
          </template>
        </section>

        <section class="section">
          <BasePageHeader
              title="Related scenarios"
              :subtitle="`${scenarios.length} scenario(s) linked to this language.`"
          />

          <div v-if="scenarios.length" class="table-wrap card table-card">
            <table class="table">
              <thead>
              <tr>
                <th>Name</th>
                <th>Author</th>
                <th>Created</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="s in scenarios" :key="s.id">
                <td>
                  <RouterLink :to="`/scenarios/${s.id}`" class="table__primary-link">
                    {{ s.title ?? "Untitled scenario" }}
                  </RouterLink>
                </td>
                <td>{{ s.authorUsername ?? "Unknown author" }}</td>
                <td>{{ s.createdAt ?? "-" }}</td>
              </tr>
              </tbody>
            </table>
          </div>

          <BaseEmptyState
              v-else
              title="No scenarios linked yet"
              message="This language is not yet associated with a scenario."
          />
        </section>
      </section>
    </template>
  </main>
</template>