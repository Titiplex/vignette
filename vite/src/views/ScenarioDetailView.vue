<script setup>
import {onMounted, ref} from "vue";
import {fetchLanguage} from "../api/languages";
import {fetchScenario, fetchScenarioThumbnails, fetchThumbnailAudios, uploadScenarioThumbnail,} from "../api/scenarios";
import {useAuth} from "../composables/useAuth";
import ThumbnailCard from "../components/ThumbnailCard.vue";
import AudioPanel from "../components/AudioPanel.vue";

const props = defineProps({
  id: {type: String, required: true},
});

const {currentUser, loadMe} = useAuth();

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
    await loadThumbs();
  } catch (e) {
    uploadError.value = e.message;
  }
}

async function refreshAudios() {
  await loadThumbs();
}

onMounted(loadAll);
</script>

<template>
  <main class="page">
    <p v-if="loading" class="muted">Loading scenario workspace...</p>
    <p v-else-if="error" class="error">{{ error }}</p>

    <template v-else-if="scenario">
      <section class="section">
        <div class="section-heading">
          <div>
            <h1>{{ scenario.title ?? "Scenario" }}</h1>
            <p class="muted">Scenario workspace and media management</p>
          </div>
        </div>

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

          <p v-if="uploadSuccess" class="success">{{ uploadSuccess }}</p>
          <p v-if="uploadError" class="error">{{ uploadError }}</p>
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

          <div v-else class="card empty-state">
            <h3>No thumbnails yet</h3>
            <p class="muted">
              Upload an image to start building the scenario.
            </p>
          </div>
        </section>

        <AudioPanel
            :selected-thumb="selectedThumb"
            :is-owner="isOwner"
            @uploaded="refreshAudios"
        />
      </section>
    </template>
  </main>
</template>