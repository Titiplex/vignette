<script setup>
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {fetchLanguage} from "../api/languages";
import {
  fetchScenario,
  fetchScenarioThumbnails,
  fetchThumbnailAudios,
  publishScenario,
  updateScenarioStoryboard,
  updateThumbnailLayout,
  uploadScenarioThumbnail,
} from "../api/scenarios";
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
const savingStoryboard = ref(false);
const savingLayout = ref(false);
const publishing = ref(false);

const uploadTitle = ref("");
const uploadFile = ref(null);
const highlightedThumbnailId = ref(null);

const storyboardForm = ref({
  layoutMode: "PRESET",
  preset: "GRID_3",
  columns: 3,
});

const selectedLayoutForm = ref({
  gridColumn: "",
  gridRow: "",
  gridColumnSpan: 1,
  gridRowSpan: 1,
});

function safeNumber(value, fallback = 0) {
  const n = Number(value);
  return Number.isFinite(n) ? n : fallback;
}

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}

function nullableInt(value) {
  if (value === "" || value === null || value === undefined) return null;
  const n = Number(value);
  return Number.isFinite(n) ? Math.trunc(n) : null;
}

function normalizeMarkers(audios) {
  return (audios || [])
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
          _x: Number.isFinite(x) ? clamp(x, 0, 100) : null,
          _y: Number.isFinite(y) ? clamp(y, 0, 100) : null,
        };
      })
      .filter((audio) => audio._x !== null && audio._y !== null);
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
    const aIdx = safeNumber(a?.idx, Number.MAX_SAFE_INTEGER);
    const bIdx = safeNumber(b?.idx, Number.MAX_SAFE_INTEGER);
    if (aIdx !== bIdx) return aIdx - bIdx;
    return safeNumber(a?.id, 0) - safeNumber(b?.id, 0);
  });
});

const selectedAudioMarkers = computed(() => normalizeMarkers(selectedAudios.value));

const playbackQueue = computed(() => {
  return sortedThumbnails.value.flatMap((thumb) => {
    const audios = [...(audioMap.value[thumb.id] || [])].sort((a, b) => {
      const aIdx = safeNumber(a?.idx, Number.MAX_SAFE_INTEGER);
      const bIdx = safeNumber(b?.idx, Number.MAX_SAFE_INTEGER);
      if (aIdx !== bIdx) return aIdx - bIdx;
      return safeNumber(a?.id, 0) - safeNumber(b?.id, 0);
    });

    return audios.map((audio) => ({
      thumbnailId: thumb.id,
      thumbnailIdx: thumb.idx ?? null,
      thumbnailTitle: thumb.title ?? "",
      audioId: audio.id,
      audioIdx: audio.idx ?? null,
      audioTitle: audio.title ?? "",
      audioUrl: buildApiUrl(`/api/audios/${audio.id}/content`),
      markerX: audio.markerX,
      markerY: audio.markerY,
      markerLabel: audio.markerLabel,
    }));
  });
});

const isPublished = computed(() => scenario.value?.visibilityStatus === "PUBLISHED");

const playerStateLabel = computed(() => {
  if (autoplay.isLoading.value) return "loading";
  if (autoplay.isPlaying.value) return "playing";
  if (autoplay.isPaused.value) return "paused";
  return "idle";
});

const storyboardColumns = computed(() => {
  const raw = storyboardForm.value.columns ?? scenario.value?.storyboardColumns ?? 3;
  return clamp(safeNumber(raw, 3), 1, 8);
});

watch(
    () => scenario.value,
    (value) => {
      if (!value) return;
      storyboardForm.value = {
        layoutMode: value.storyboardLayoutMode ?? "PRESET",
        preset: value.storyboardPreset ?? "GRID_3",
        columns: value.storyboardColumns ?? 3,
      };
    },
    {immediate: true}
);

watch(
    () => selectedThumb.value,
    (thumb) => {
      if (!thumb) {
        selectedLayoutForm.value = {gridColumn: "", gridRow: "", gridColumnSpan: 1, gridRowSpan: 1};
        return;
      }

      selectedLayoutForm.value = {
        gridColumn: thumb.gridColumn ?? "",
        gridRow: thumb.gridRow ?? "",
        gridColumnSpan: thumb.gridColumnSpan ?? 1,
        gridRowSpan: thumb.gridRowSpan ?? 1,
      };
    },
    {immediate: true}
);

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
  highlightedThumbnailId.value = item.thumbnailId;

  nextTick(() => {
    const el = document.querySelector(`[data-thumbnail-id="${item.thumbnailId}"]`);
    el?.scrollIntoView({behavior: "smooth", block: "center"});
  });

  window.clearTimeout(focusPlaybackItem._highlightTimeout);
  focusPlaybackItem._highlightTimeout = window.setTimeout(() => {
    if (String(highlightedThumbnailId.value) === String(item.thumbnailId)) {
      highlightedThumbnailId.value = null;
    }
  }, 1400);
}

const autoplay = useScenarioAutoplay(playbackQueue, {
  gapMs: 320,
  autoContinue: true,
  loopScenario: false,
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

function toggleAutoContinue() {
  autoplay.toggleAutoContinue();
  toast.info(
      autoplay.autoContinue.value
          ? "Auto-continue enabled."
          : "Auto-continue disabled."
  );
}

function toggleLoopScenario() {
  autoplay.toggleLoopScenario();
  toast.info(
      autoplay.loopScenario.value
          ? "Loop scenario enabled."
          : "Loop scenario disabled."
  );
}

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

async function setActiveAudio(audio) {
  if (!audio || !selectedThumb.value) {
    autoplay.stop();
    activeAudioId.value = null;
    return;
  }

  const idx = playbackQueue.value.findIndex(
      (item) =>
          String(item.thumbnailId) === String(selectedThumb.value.id) &&
          String(item.audioId) === String(audio.id)
  );

  if (idx >= 0) {
    await autoplay.playFromIndex(idx);
  } else {
    activeAudioId.value = audio?.id ?? null;
  }
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
        } catch (e) {
            console.error(`Failed to load audios for thumbnail ${t.id}`, e);
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

async function publishCurrentScenario() {
  if (!scenario.value || publishing.value) return;

  publishing.value = true;
  try {
    scenario.value = await publishScenario(props.id);
    toast.success("Scenario published.");
  } catch (e) {
    toast.error(e.message || "Failed to publish scenario.");
  } finally {
    publishing.value = false;
  }
}

async function saveStoryboardSettings() {
  if (!scenario.value || savingStoryboard.value) return;

  savingStoryboard.value = true;
  try {
    scenario.value = await updateScenarioStoryboard(props.id, {
      layoutMode: storyboardForm.value.layoutMode,
      preset: storyboardForm.value.preset,
      columns: Number(storyboardForm.value.columns),
    });
    toast.success("Storyboard settings saved.");
  } catch (e) {
    toast.error(e.message || "Failed to save storyboard settings.");
  } finally {
    savingStoryboard.value = false;
  }
}

async function saveSelectedThumbnailLayout() {
  if (!selectedThumb.value || savingLayout.value) return;

  savingLayout.value = true;
  try {
    await updateThumbnailLayout(selectedThumb.value.id, {
      gridColumn: nullableInt(selectedLayoutForm.value.gridColumn),
      gridRow: nullableInt(selectedLayoutForm.value.gridRow),
      gridColumnSpan: nullableInt(selectedLayoutForm.value.gridColumnSpan) ?? 1,
      gridRowSpan: nullableInt(selectedLayoutForm.value.gridRowSpan) ?? 1,
    });

    await loadThumbs();
    const refreshed = thumbnails.value.find((thumb) => String(thumb.id) === String(selectedThumb.value.id));
    if (refreshed) {
      selectedThumb.value = refreshed;
    }

    toast.success("Thumbnail layout saved.");
  } catch (e) {
    toast.error(e.message || "Failed to save thumbnail layout.");
  } finally {
    savingLayout.value = false;
  }
}

async function refreshAudios() {
  await loadThumbs();
}

function defaultTileLayout(thumb, index) {
  const columns = storyboardColumns.value;
  const preset = (scenario.value?.storyboardPreset ?? "GRID_3").toUpperCase();

  if (preset === "GRID_2") {
    return {
      columnStart: null,
      columnSpan: Math.min(columns, index % 4 === 0 ? 2 : 1),
      rowStart: null,
      rowSpan: 1,
    };
  }

  if (preset === "CINEMATIC") {
    return {
      columnStart: null,
      columnSpan: Math.min(columns, index === 0 ? 2 : 1),
      rowStart: null,
      rowSpan: index === 1 ? 2 : 1,
    };
  }

  if (preset === "MANGA") {
    return {
      columnStart: null,
      columnSpan: Math.min(columns, index % 5 === 0 ? 2 : 1),
      rowStart: null,
      rowSpan: index % 3 === 1 ? 2 : 1,
    };
  }

  const portrait = safeNumber(thumb?.imageHeight, 0) > safeNumber(thumb?.imageWidth, 0);

  return {
    columnStart: null,
    columnSpan: Math.min(columns, index === 0 && columns >= 3 ? 2 : 1),
    rowStart: null,
    rowSpan: portrait ? 2 : 1,
  };
}

const storyboardItems = computed(() => {
  const layoutMode = (scenario.value?.storyboardLayoutMode ?? "PRESET").toUpperCase();

  return sortedThumbnails.value.map((thumb, index) => {
    if (layoutMode === "CUSTOM") {
      return {
        ...thumb,
        _layout: {
          columnStart: thumb.gridColumn ?? null,
          columnSpan: thumb.gridColumnSpan ?? 1,
          rowStart: thumb.gridRow ?? null,
          rowSpan: thumb.gridRowSpan ?? 1,
        },
      };
    }

    return {
      ...thumb,
      _layout: defaultTileLayout(thumb, index),
    };
  });
});

function storyboardItemStyle(item) {
  const layout = item?._layout ?? {columnStart: null, columnSpan: 1, rowStart: null, rowSpan: 1};

  return {
    gridColumn: layout.columnStart
        ? `${layout.columnStart} / span ${layout.columnSpan}`
        : `span ${layout.columnSpan}`,
    ...(layout.rowStart
        ? {gridRow: `${layout.rowStart} / span ${layout.rowSpan}`}
        : (layout.rowSpan > 1 ? {gridRow: `span ${layout.rowSpan}`} : {})),
  };
}

onMounted(loadAll);
</script>

<template>
  <main class="page">
    <BaseLoader v-if="loading">Loading storyboard...</BaseLoader>

    <BaseAlert v-else-if="error" type="error">
      {{ error }}
    </BaseAlert>

    <template v-else-if="scenario">
      <section class="section">
        <BasePageHeader
            :title="scenario.title ?? 'Scenario'"
            subtitle="Storyboard workspace with publication, layout and media controls"
        >
          <template #actions>
            <BaseBadge :variant="isPublished ? 'success' : 'warning'">
              {{ scenario.visibilityStatus }}
            </BaseBadge>

            <BaseBadge :variant="isOwner ? 'success' : 'neutral'">
              {{ isOwner ? "Owner view" : "Read-only" }}
            </BaseBadge>

            <button
                v-if="isOwner && !isPublished"
                class="btn btn--primary"
                :disabled="publishing"
                @click="publishCurrentScenario"
            >
              {{ publishing ? "Publishing..." : "Publish scenario" }}
            </button>
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
              <div>
                <h3>Status</h3>
                <p>{{ scenario.visibilityStatus }}</p>
              </div>
            </div>

            <section class="card">
              <h2>Description</h2>
              <p class="text">{{ scenario.description ?? "No description available." }}</p>
            </section>

            <section v-if="isOwner" class="card form-card--premium">
              <h2>Publication & storyboard</h2>

              <BaseAlert v-if="!isPublished" type="info">
                This scenario is still private. Public users cannot see it until you publish it.
              </BaseAlert>

              <div class="storyboard-settings-grid">
                <label>
                  Layout mode
                  <select v-model="storyboardForm.layoutMode">
                    <option value="PRESET">Preset</option>
                    <option value="CUSTOM">Custom</option>
                  </select>
                </label>

                <label>
                  Preset
                  <select v-model="storyboardForm.preset" :disabled="storyboardForm.layoutMode !== 'PRESET'">
                    <option value="GRID_3">Grid 3</option>
                    <option value="GRID_2">Grid 2</option>
                    <option value="CINEMATIC">Cinematic</option>
                    <option value="MANGA">Manga</option>
                  </select>
                </label>

                <label>
                  Columns
                  <input v-model="storyboardForm.columns" type="number" min="1" max="8"/>
                </label>
              </div>

              <div class="toolbar">
                <button class="btn btn--primary" :disabled="savingStoryboard" @click="saveStoryboardSettings">
                  {{ savingStoryboard ? "Saving..." : "Save storyboard settings" }}
                </button>
              </div>

              <p class="muted storyboard-help">
                Preset mode auto-composes the storyboard. Custom mode uses each thumbnail’s saved grid position and
                spans.
              </p>
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
                  title="Storyboard"
                  :subtitle="`${thumbnails.length} thumbnail(s) in this scenario.`"
              />

              <div
                  v-if="storyboardItems.length"
                  class="storyboard-grid"
                  :style="{ '--storyboard-columns': storyboardColumns }"
              >
                <ThumbnailCard
                    v-for="item in storyboardItems"
                    :key="item.id"
                    :thumb="item"
                    :audios="audioMap[item.id] || []"
                    :selected="selectedThumb?.id === item.id"
                    :highlighted="highlightedThumbnailId === item.id"
                    :style="storyboardItemStyle(item)"
                    @select="selectThumb"
                />
              </div>

              <BaseEmptyState
                  v-else
                  title="No thumbnails yet"
                  message="Upload an image to start building the storyboard."
              />
            </section>
          </div>

          <aside class="scenario-layout__side">
            <section class="card autoplay-panel">
              <div class="autoplay-panel__header">
                <div>
                  <h2>Scenario player</h2>
                  <p class="muted">
                    Automatic playback through all audio clips in thumbnail order.
                  </p>
                </div>

                <BaseBadge variant="info">
                  {{
                    autoplay.currentIndex >= 0 ? `${autoplay.currentIndex + 1}/${playbackQueue.length}` : `0/${playbackQueue.length}`
                  }}
                </BaseBadge>
              </div>

              <div class="transport-card">
                <div class="transport-card__top">
                  <div>
                    <p class="transport-card__title">
                      <template v-if="autoplay.currentItem">
                        {{ autoplay.currentItem.audioTitle || `Audio #${autoplay.currentItem.audioId}` }}
                      </template>
                      <template v-else>
                        No audio selected
                      </template>
                    </p>

                    <p class="muted transport-card__subtitle">
                      <template v-if="autoplay.currentItem">
                        Thumbnail #{{ autoplay.currentItem.thumbnailIdx ?? autoplay.currentItem.thumbnailId }}
                        <span v-if="autoplay.currentItem.audioIdx != null">
                          · Audio order {{ autoplay.currentItem.audioIdx }}
                        </span>
                        · {{ playerStateLabel }}
                      </template>
                      <template v-else>
                        Idle
                      </template>
                    </p>
                  </div>

                  <div class="transport-toggles">
                    <button
                        type="button"
                        class="btn"
                        :class="autoplay.autoContinue ? 'btn--primary' : 'btn--ghost'"
                        @click="toggleAutoContinue"
                    >
                      Auto-continue {{ autoplay.autoContinue ? "On" : "Off" }}
                    </button>

                    <button
                        type="button"
                        class="btn"
                        :class="autoplay.loopScenario ? 'btn--primary' : 'btn--ghost'"
                        @click="toggleLoopScenario"
                    >
                      Loop {{ autoplay.loopScenario ? "On" : "Off" }}
                    </button>
                  </div>
                </div>

                <div class="transport-progress">
                  <input
                      type="range"
                      min="0"
                      max="100"
                      step="0.1"
                      :value="autoplay.progressPercent"
                      @input="autoplay.seekToPercent($event.target.value)"
                  />
                  <div class="transport-progress__times">
                    <span>{{ autoplay.formatTime(autoplay.currentTime) }}</span>
                    <span>{{ autoplay.formatTime(autoplay.duration) }}</span>
                  </div>
                </div>

                <div class="transport-controls">
                  <button
                      type="button"
                      class="btn btn--ghost"
                      :disabled="!playbackQueue.length"
                      @click="autoplay.previous"
                  >
                    Previous
                  </button>

                  <button
                      type="button"
                      class="btn btn--ghost"
                      :disabled="!playbackQueue.length"
                      @click="autoplay.replayCurrent"
                  >
                    Replay
                  </button>

                  <button
                      v-if="!autoplay.isPlaying"
                      type="button"
                      class="btn btn--primary"
                      :disabled="!playbackQueue.length || autoplay.isLoading"
                      @click="autoplay.isPaused ? autoplay.resume() : playAllFromContext()"
                  >
                    {{ autoplay.isPaused ? "Resume" : "Play" }}
                  </button>

                  <button
                      v-else
                      type="button"
                      class="btn btn--primary"
                      @click="autoplay.pause"
                  >
                    Pause
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
              </div>
            </section>

            <section v-if="selectedThumb" class="card selected-preview selected-preview--premium">
              <h2>Selected thumbnail</h2>

              <div class="selected-preview__stage selected-preview__stage--storyboard">
                <img
                    :src="thumbnailContentUrl(selectedThumb)"
                    :alt="selectedThumb.title || 'Selected thumbnail'"
                    class="selected-preview__image selected-preview__image--storyboard"
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

            <section v-if="isOwner && selectedThumb" class="card">
              <h2>Selected thumbnail layout</h2>

              <div class="storyboard-settings-grid">
                <label>
                  Column
                  <input v-model="selectedLayoutForm.gridColumn" type="number" min="1" placeholder="auto"/>
                </label>

                <label>
                  Row
                  <input v-model="selectedLayoutForm.gridRow" type="number" min="1" placeholder="auto"/>
                </label>

                <label>
                  Column span
                  <input v-model="selectedLayoutForm.gridColumnSpan" type="number" min="1"/>
                </label>

                <label>
                  Row span
                  <input v-model="selectedLayoutForm.gridRowSpan" type="number" min="1"/>
                </label>
              </div>

              <div class="toolbar">
                <button class="btn btn--primary" :disabled="savingLayout" @click="saveSelectedThumbnailLayout">
                  {{ savingLayout ? "Saving..." : "Save thumbnail layout" }}
                </button>
              </div>

              <p class="muted">
                In custom mode, these values control the persisted storyboard composition for this thumbnail.
              </p>
            </section>
          </aside>
        </div>

        <AudioPanel
            :selected-thumb="selectedThumb"
            :audios="selectedAudios"
            :active-audio-id="activeAudioId"
            :active-audio-title="autoplay.currentItem?.audioTitle ?? ''"
            :player-state="playerStateLabel"
            :is-owner="isOwner"
            @uploaded="refreshAudios"
            @play-audio="setActiveAudio"
        />
      </section>
    </template>
  </main>
</template>