<script setup>
import {computed, onMounted, ref} from "vue";
import {fetchLanguage} from "../api/languages";
import {fetchScenario, fetchScenarioThumbnails, fetchThumbnailAudios, uploadScenarioThumbnail,} from "../api/scenarios";
import {useAuth} from "../composables/useAuth";
import {useToast} from "../composables/useToast";
import ThumbnailCard from "../components/ThumbnailCard.vue";
import AudioPanel from "../components/AudioPanel.vue";
import BasePageHeader from "../components/ui/BasePageHeader.vue";
import BaseLoader from "../components/ui/BaseLoader.vue";
import BaseAlert from "../components/ui/BaseAlert.vue";
import BaseEmptyState from "../components/ui/BaseEmptyState.vue";

const props = defineProps({
  id: {type: String, required: true},
});

const {currentUser, loadMe} = useAuth();
const toast = useToast();

const scenario = ref(null);
const languageName = ref("");
const thumbnails = ref([]);
const audioMap = ref({});
const selectedThumb = ref(null);
const error = ref("");
const uploadError = ref("");
const uploadSuccess = ref("");
const isOwner = ref(false);
const loading = ref(false);

const uploadTitle = ref("");
const uploadFile = ref(null);

const selectedAudios = computed(() => {
  if (!selectedThumb.value) return [];
  return audioMap.value[selectedThumb.value.id] || [];
});

function onImageChange(event) {
  uploadFile.value = event.target.files?.[0] ?? null;
}

async function loadScenario() {
  scenario.value = await fetchScenario(props.id);

  if (scenario.value?.languageId) {
    try {
      const lang = await fetchLanguage(scenario.value.languageId);
      languageName.value = lang.name ?? "Unknown language";
    } catch {
      languageName.value = "Unknown language";
    }
  } else {
    languageName.value = "-";
  }
}

async function loadThumbs() {
  thumbnails.value = await fetchScenarioThumbnails(props.id);

  const map = {};
  await Promise.all(
      thumbnails.value.map(async (t) => {
        try {
          map[t.id] = await fetchThumbnailAudios(t.id);
        } catch {
          map[t.id] = [];
        }
      })
  );

  audioMap.value = map;

  if (!selectedThumb.value && thumbnails.value.length) {
    selectedThumb.value = thumbnails.value[0];
  } else if (
      selectedThumb.value &&
      !thumbnails.value.some((t) => t.id === selectedThumb.value.id)
  ) {
    selectedThumb.value = thumbnails.value[0] || null;
  }
}

async function loadAll() {
  loading.value = true;
  error.value = "";

  try {
    await loadMe();
    await loadScenario();

    isOwner.value =
        !!currentUser.value &&
        currentUser.value.username === scenario.value.authorUsername;

    await loadThumbs();
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}

function selectThumb(thumb) {
  selectedThumb.value = thumb;
}

async function uploadImage() {
  uploadError.value = "";
  uploadSuccess.value = "";

  try {
    if (!uploadFile.value) throw new Error("No image selected.");

    const fd = new FormData();
    fd.append("scenarioId", String(scenario.value.id));
    fd.append("title", uploadTitle.value || "");
    fd.append("image", uploadFile.value);

    await uploadScenarioThumbnail(props.id, fd);

    uploadTitle.value = "";
    uploadFile.value = null;
    uploadSuccess.value = "Image uploaded successfully.";
    toast.success("Thumbnail uploaded successfully.");
    await loadThumbs();
  } catch (e) {
    uploadError.value = e.message;
    toast.error(e.message || "Upload failed.");
  }
}

async function refreshAudios() {
  await loadThumbs();
}

onMounted(loadAll);
</script>

<template>
  <main class="page">
    <BaseLoader v-if="loading">Loading scenario workspace...</BaseLoader>

    <BaseAlert v-else-if="error" type="error">
      {{ error }}
    </BaseAlert>

    <template v-else-if="scenario">
      <section class="section">
        <BasePageHeader
            :title="scenario.title ?? 'Scenario'"
            subtitle="Scenario workspace and media management"
        />

        <div class="scenario-layout">
          <div class="scenario-layout__main">
            <div class="card info-grid">
              <div>
                <h3>Language</h3>
                <p>{{ languageName }}</p>
              </div>
              <div>
                <h3>Author</h3>
                <p>{{ scenario.authorUsername ?? "-" }}</p>
              </div>
              <div>
                <h3>Status</h3>
                <p>{{ isOwner ? "Owner view" : "Read-only view" }}</p>
              </div>
            </div>

            <section class="card">
              <h2>Description</h2>
              <p class="text">{{ scenario.description ?? "No description available." }}</p>
            </section>

            <section v-if="isOwner" class="card">
              <h2>Add a thumbnail</h2>

              <div class="form-grid">
                <label>
                  Title
                  <input v-model="uploadTitle" placeholder="Optional image title"/>
                </label>

                <label>
                  Image file
                  <input type="file" accept="image/*" @change="onImageChange"/>
                </label>
              </div>

              <div class="toolbar">
                <button class="btn btn--primary" @click="uploadImage">
                  Upload image
                </button>
              </div>

              <BaseAlert v-if="uploadSuccess" type="success">
                {{ uploadSuccess }}
              </BaseAlert>

              <BaseAlert v-if="uploadError" type="error">
                {{ uploadError }}
              </BaseAlert>
            </section>

            <section class="section">
              <div class="section-heading">
                <div>
                  <h2>Thumbnails</h2>
                  <p class="muted">
                    {{ thumbnails.length }} thumbnail(s) available in this scenario.
                  </p>
                </div>
              </div>

              <div v-if="thumbnails.length" class="card-grid">
                <ThumbnailCard
                    v-for="thumb in thumbnails"
                    :key="thumb.id"
                    :thumb="thumb"
                    :audios="audioMap[thumb.id] || []"
                    :selected="selectedThumb?.id === thumb.id"
                    @select="selectThumb"
                />
              </div>

              <BaseEmptyState
                  v-else
                  title="No thumbnails yet"
                  message="Upload an image to start building the scenario."
              />
            </section>
          </div>

          <aside class="scenario-layout__side">
            <section v-if="selectedThumb" class="card selected-preview">
              <h2>Selected thumbnail</h2>
              <img
                  :src="`/api/thumbnails/${selectedThumb.id}/content`"
                  :alt="selectedThumb.title || 'Selected thumbnail'"
                  class="selected-preview__image"
              />
              <p class="muted">
                {{ selectedThumb.title || `Thumbnail #${selectedThumb.idx ?? selectedThumb.id}` }}
              </p>
              <p class="muted">
                {{ selectedAudios.length }} audio clip(s) attached
              </p>
            </section>

            <AudioPanel
                :selected-thumb="selectedThumb"
                :is-owner="isOwner"
                @uploaded="refreshAudios"
            />
          </aside>
        </div>
      </section>
    </template>
  </main>
</template>