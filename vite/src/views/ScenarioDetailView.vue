<script setup>
import {computed, onMounted, ref} from "vue";
import {fetchLanguage} from "../api/languages";
import {fetchScenario, fetchScenarioThumbnails, fetchThumbnailAudios, uploadScenarioThumbnail,} from "../api/scenarios";
import {buildApiUrl} from "../api/rest";
import {useAuth} from "../composables/useAuth";
import {useToast} from "../composables/useToast";
import ThumbnailCard from "../components/ThumbnailCard.vue";
import AudioPanel from "../components/AudioPanel.vue";
import BasePageHeader from "../components/ui/BasePageHeader.vue";
import BaseLoader from "../components/ui/BaseLoader.vue";
import BaseAlert from "../components/ui/BaseAlert.vue";
import BaseEmptyState from "../components/ui/BaseEmptyState.vue";
import BaseBadge from "../components/ui/BaseBadge.vue";

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
const activeAudioId = ref(null);
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

const selectedAudioMarkers = computed(() => {
  return selectedAudios.value
      .filter((audio) =>
          audio?.markerX !== null &&
          audio?.markerX !== undefined &&
          audio?.markerY !== null &&
          audio?.markerY !== undefined &&
          audio?.markerX !== "" &&
          audio?.markerY !== ""
      )
      .map((audio) => {
        const x = Number(audio.markerX);
        const y = Number(audio.markerY);
        return {
          ...audio,
          _x: Number.isFinite(x) ? Math.max(0, Math.min(100, x)) : null,
          _y: Number.isFinite(y) ? Math.max(0, Math.min(100, y)) : null,
        };
      })
      .filter((audio) => audio._x !== null && audio._y !== null);
});

function onImageChange(event) {
  uploadFile.value = event.target.files?.[0] ?? null;
}

function thumbnailContentUrl(thumb) {
  if (!thumb?.id) return "";
  return buildApiUrl(`/api/thumbnails/${thumb.id}/content`);
}

function markerStyle(audio) {
  return {
    left: `${audio._x}%`,
    top: `${audio._y}%`,
  };
}

function isMarkerActive(audio) {
  return String(activeAudioId.value ?? "") === String(audio?.id ?? "");
}

function selectThumb(thumb) {
  selectedThumb.value = thumb;
  activeAudioId.value = null;
}

function setActiveAudio(audio) {
  activeAudioId.value = audio?.id ?? null;
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

  if (
      activeAudioId.value != null &&
      !selectedAudios.value.some((audio) => String(audio.id) === String(activeAudioId.value))
  ) {
    activeAudioId.value = null;
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
        >
          <template #actions>
            <BaseBadge :variant="isOwner ? 'success' : 'neutral'">
              {{ isOwner ? "Owner view" : "Read-only" }}
            </BaseBadge>
          </template>
        </BasePageHeader>

        <div class="scenario-layout">
          <div class="scenario-layout__main">
            <div class="card info-grid info-grid--premium">
              <div>
                <h3>Language</h3>
                <p>{{ languageName }}</p>
              </div>
              <div>
                <h3>Author</h3>
                <p>{{ scenario.authorUsername ?? "-" }}</p>
              </div>
              <div>
                <h3>Thumbnails</h3>
                <p>{{ thumbnails.length }}</p>
              </div>
            </div>

            <section class="card">
              <h2>Description</h2>
              <p class="text">{{ scenario.description ?? "No description available." }}</p>
            </section>

            <section v-if="isOwner" class="card form-card--premium">
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
              <BasePageHeader
                  title="Thumbnails"
                  :subtitle="`${thumbnails.length} thumbnail(s) available in this scenario.`"
              />

              <div v-if="thumbnails.length" class="card-grid card-grid--thumbs">
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
            <section v-if="selectedThumb" class="card selected-preview selected-preview--premium">
              <h2>Selected thumbnail</h2>

              <div class="selected-preview__stage">
                <img
                    :src="thumbnailContentUrl(selectedThumb)"
                    :alt="selectedThumb.title || 'Selected thumbnail'"
                    class="selected-preview__image"
                />

                <button
                    v-for="audio in selectedAudioMarkers"
                    :key="audio.id"
                    type="button"
                    class="marker-dot"
                    :class="{ 'marker-dot--active': isMarkerActive(audio) }"
                    :style="markerStyle(audio)"
                    :title="audio.markerLabel || audio.title || `Audio #${audio.id}`"
                >
                  <span class="marker-dot__pulse"></span>
                  <span class="marker-dot__core"></span>
                </button>
              </div>

              <div class="meta-badges">
                <BaseBadge variant="info">
                  #{{ selectedThumb.idx ?? selectedThumb.id }}
                </BaseBadge>
                <BaseBadge variant="neutral">
                  {{ selectedAudios.length }} audio clip(s)
                </BaseBadge>
                <BaseBadge variant="warning">
                  {{ selectedAudioMarkers.length }} marker(s)
                </BaseBadge>
              </div>

              <p class="muted selected-preview__title">
                {{ selectedThumb.title || `Thumbnail #${selectedThumb.idx ?? selectedThumb.id}` }}
              </p>
            </section>

            <AudioPanel
                :selected-thumb="selectedThumb"
                :audios="selectedAudios"
                :active-audio-id="activeAudioId"
                :is-owner="isOwner"
                @uploaded="refreshAudios"
                @play-audio="setActiveAudio"
            />
          </aside>
        </div>
      </section>
    </template>
  </main>
</template>