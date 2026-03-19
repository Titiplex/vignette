<script setup>
import {onMounted, ref} from "vue";
import {apiFetch} from "../api/rest";
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
const isOwner = ref(false);

const uploadTitle = ref("");
const uploadFile = ref(null);

function onImageChange(event) {
  uploadFile.value = event.target.files?.[0] ?? null;
}

async function loadScenario() {
  scenario.value = await apiFetch(`/api/scenarios/${props.id}`);

  if (scenario.value?.languageId) {
    try {
      const lang = await apiFetch(`/api/languages/${scenario.value.languageId}`);
      languageName.value = lang.name ?? "Unknown language";
    } catch {
      languageName.value = "Unknown language";
    }
  }
}

async function loadThumbs() {
  thumbnails.value = await apiFetch(`/api/scenarios/${props.id}/thumbnails`);

  const map = {};
  await Promise.all(
      thumbnails.value.map(async (t) => {
        try {
          map[t.id] = await apiFetch(`/api/thumbnails/${t.id}/audios`);
        } catch {
          map[t.id] = [];
        }
      })
  );
  audioMap.value = map;
}

async function loadAll() {
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
  }
}

function selectThumb(thumb) {
  selectedThumb.value = thumb;
}

async function uploadImage() {
  uploadError.value = "";
  try {
    if (!uploadFile.value) throw new Error("No image selected");

    const fd = new FormData();
    fd.append("scenarioId", String(scenario.value.id));
    fd.append("title", uploadTitle.value || "");
    fd.append("image", uploadFile.value);

    await apiFetch(`/api/scenarios/${props.id}/thumbnails`, {
      method: "POST",
      body: fd,
    });

    uploadTitle.value = "";
    uploadFile.value = null;
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
    <p v-if="error" class="error">{{ error }}</p>

    <template v-else-if="scenario">
      <h1>Scenario Workspace</h1>
      <h2>{{ scenario.title ?? "Scenario" }}</h2>

      <p><strong>Language:</strong> {{ languageName }}</p>
      <p><strong>Author:</strong> {{ scenario.authorUsername ?? "" }}</p>
      <p>{{ scenario.description ?? "" }}</p>

      <section v-if="isOwner" class="card">
        <h2>Add an image</h2>

        <label>
          Title
          <input v-model="uploadTitle"/>
        </label>

        <label>
          Image
          <input type="file" accept="image/*" @change="onImageChange"/>
        </label>

        <button @click="uploadImage">Upload image</button>
        <p v-if="uploadError" class="error">{{ uploadError }}</p>
      </section>

      <section>
        <h2>Thumbnails</h2>

        <div class="card-grid">
          <ThumbnailCard
              v-for="thumb in thumbnails"
              :key="thumb.id"
              :thumb="thumb"
              :audios="audioMap[thumb.id] || []"
              :selected="selectedThumb?.id === thumb.id"
              @select="selectThumb"
          />
        </div>
      </section>

      <AudioPanel
          :selected-thumb="selectedThumb"
          :is-owner="isOwner"
          @uploaded="refreshAudios"
      />
    </template>
  </main>
</template>