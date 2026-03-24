<script setup>
import {computed, nextTick, onMounted, ref} from "vue";
import {fetchLanguage} from "../api/languages";
import {fetchScenario, fetchScenarioThumbnails, fetchThumbnailAudios, uploadScenarioThumbnail,} from "../api/scenarios";
import {buildApiUrl} from "../api/rest";
import {useAuth} from "../composables/useAuth";
import {useToast} from "../composables/useToast";
import {useScenarioAutoplay} from "../composables/useScenarioAutoplay";
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

function safeNumber(value, fallback = 0) {
  const n = Number(value);
  return Number.isFinite(n) ? n : fallback;
}

const sortedThumbnails = computed(() => {
  return [...thumbnails.value].sort((a, b) => {
    const aIdx = safeNumber(a?.idx, Number.MAX_SAFE_INTEGER);
    const bIdx = safeNumber(b?.idx, Number.MAX_SAFE_INTEGER);
    if (aIdx !== bIdx) return aIdx - bIdx;
    return safeNumber(a?.id, 0) - safeNumber(b?.id, 0);
  });
});

const selectedAudios = computed(() => {
  if (!selectedThumb.value) return [];

  return [...(audioMap.value[selectedThumb.value.id] || [])].sort((a, b) => {
    return safeNumber(a?.id, 0) - safeNumber(b?.id, 0);
  });
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

const playbackQueue = computed(() => {
  return sortedThumbnails.value.flatMap((thumb) => {
    const audios = [...(audioMap.value[thumb.id] || [])].sort((a, b) => {
      return safeNumber(a?.id, 0) - safeNumber(b?.id, 0);
    });

    return audios.map((audio) => ({
      thumbnailId: thumb.id,
      thumbnailIdx: thumb.idx ?? null,
      thumbnailTitle: thumb.title ?? "",
      audioId: audio.id,
      audioTitle: audio.title ?? "",
      audioUrl: buildApiUrl(`/api/audios/${audio.id}/content`),
      markerX: audio.markerX,
      markerY: audio.markerY,
      markerLabel: audio.markerLabel,
    }));
  });
});

function markerStyle(audio) {
  return {
    left: `${audio._x}%`,
    top: `${audio._y}%`,
  };
}

function isMarkerActive(audio) {
  return String(activeAudioId.value ?? "") === String(audio?.id ?? "");
}

function onImageChange(event) {
  uploadFile.value = event.target.files?.[0] ?? null;
}

function thumbnailContentUrl(thumb) {
  if (!thumb?.id) return "";
  return buildApiUrl(`/api/thumbnails/${thumb.id}/content`);
}

function selectThumb(thumb) {
  selectedThumb.value = thumb;
  activeAudioId.value = null;
}

function focusPlaybackItem(item) {
  const thumb = thumbnails.value.find((t) => String(t.id) === String(item.thumbnailId)) ?? null;

  selectedThumb.value = thumb;
  activeAudioId.value = item.audioId ?? null;

  nextTick(() => {
    const el = document.querySelector(`[data-thumbnail-id="${item.thumbnailId}"]`);
    el?.scrollIntoView({behavior: "smooth", block: "center"});
  });
}

const autoplay = useScenarioAutoplay(playbackQueue, {
  gapMs: 250,
  onItemChange: (item) => {
    focusPlaybackItem(item);
  },
  onStop: () => {
    activeAudioId.value = null;
  },
  onEndedAll: () => {
    activeAudioId.value = null;
    toast.success("Automatic playback finished.");
  },
});

function findStartIndex() {
  if (!playbackQueue.value.length) return 0;

  if (selectedThumb.value && activeAudioId.value != null) {
    const exactIndex = playbackQueue.value.findIndex(
        (item) =>
            String(item.thumbnailId) === String(selectedThumb.value.id) &&
            String(item.audioId) === String(activeAudioId.value)
    );
    if (exactIndex >= 0) return exactIndex;
  }

  if (selectedThumb.value) {
    const thumbIndex = playbackQueue.value.findIndex(
        (item) => String(item.thumbnailId) === String(selectedThumb.value.id)
    );
    if (thumbIndex >= 0) return thumbIndex;
  }

  return 0;
}

async function playAllFromContext() {
  if (!playbackQueue.value.length) return;
  await autoplay.playFromIndex(findStartIndex());
}

function setActiveAudio(audio) {
  autoplay.stop();
  activeAudioId.value = audio?.id ?? null;
}

async function playAudioFromMarker(audio) {
  const idx = playbackQueue.value.findIndex(
      (item) =>
          String(item.thumbnailId) === String(selectedThumb.value?.id) &&
          String(item.audioId) === String(audio.id)
  );

  if (idx >= 0) {
    await autoplay.playFromIndex(idx);
  }
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

  if (!selectedThumb.value && sortedThumbnails.value.length) {
    selectedThumb.value = sortedThumbnails.value[0];
  } else if (
      selectedThumb.value &&
      !thumbnails.value.some((t) => t.id === selectedThumb.value.id)
  ) {
    selectedThumb.value = sortedThumbnails.value[0] || null;
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
              <div>
                <h3>Audio clips</h3>
                <p>{{ playbackQueue.length }}</p>
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

              <div v-if="sortedThumbnails.length" class="card-grid card-grid--thumbs">
                <ThumbnailCard
                    v-for="thumb in sortedThumbnails"
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
            <section class="card autoplay-panel">
              <div class="autoplay-panel__header">
                <div>
                  <h2>Automatic playback</h2>
                  <p class="muted">
                    Reads all audio clips one after another in thumbnail order.
                  </p>
                </div>

                <BaseBadge variant="info">
                  {{
                    autoplay.currentIndex >= 0 ? `${autoplay.currentIndex + 1}/${playbackQueue.length}` : `0/${playbackQueue.length}`
                  }}
                </BaseBadge>
              </div>

              <div class="autoplay-panel__actions">
                <button
                    type="button"
                    class="btn btn--primary"
                    :disabled="!playbackQueue.length || autoplay.isLoading"
                    @click="playAllFromContext"
                >
                  Play all
                </button>

                <button
                    type="button"
                    class="btn btn--ghost"
                    :disabled="!autoplay.isPlaying"
                    @click="autoplay.pause"
                >
                  Pause
                </button>

                <button
                    type="button"
                    class="btn btn--ghost"
                    :disabled="!autoplay.isPaused"
                    @click="autoplay.resume"
                >
                  Resume
                </button>

                <button
                    type="button"
                    class="btn btn--ghost"
                    :disabled="!playbackQueue.length"
                    @click="autoplay.next"
                >
                  Next
                </button>

                <button
                    type="button"
                    class="btn btn--ghost"
                    :disabled="autoplay.currentIndex < 0"
                    @click="autoplay.stop"
                >
                  Stop
                </button>
              </div>

              <p class="muted autoplay-panel__status">
                <template v-if="autoplay.currentItem">
                  Current:
                  <strong>
                    {{ autoplay.currentItem.audioTitle || `Audio #${autoplay.currentItem.audioId}` }}
                  </strong>
                  · thumbnail
                  <strong>
                    #{{ autoplay.currentItem.thumbnailIdx ?? autoplay.currentItem.thumbnailId }}
                  </strong>
                </template>
                <template v-else>
                  No automatic playback running.
                </template>
              </p>
            </section>

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
                    @click="playAudioFromMarker(audio)"
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