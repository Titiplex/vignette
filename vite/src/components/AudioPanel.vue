<script setup>
import {computed, ref, watch} from "vue";
import {updateAudioMarker, uploadThumbnailAudio} from "../api/scenarios";
import {buildApiUrl} from "../api/rest";
import {useToast} from "../composables/useToast";
import BaseBadge from "./ui/BaseBadge.vue";
import DiscussionThread from "./community/DiscussionThread.vue";

const props = defineProps({
  selectedThumb: {type: Object, default: null},
  audios: {type: Array, default: () => []},
  isOwner: {type: Boolean, default: false},
  activeAudioId: {type: [Number, String, null], default: null},
  activeAudioTitle: {type: String, default: ""},
  playerState: {type: String, default: "idle"}, // idle | playing | paused | loading
});

const emit = defineEmits([
  "uploaded",
  "play-audio",
]);

const toast = useToast();

const audioTitle = ref("");
const audioFile = ref(null);
const audioErr = ref("");
const audioSuccess = ref("");
const markerX = ref("");
const markerY = ref("");
const markerLabel = ref("");
const isRecording = ref(false);
const focusedDiscussionAudioId = ref(null);
const markerEditAudioId = ref(null);
const markerEditorOpen = ref(false);

const panelOpen = ref(false);

function togglePanel() {
  panelOpen.value = !panelOpen.value;
}

let mediaRecorder = null;
let chunks = [];
let recordedBlob = null;

function clearMarker() {
  markerX.value = "";
  markerY.value = "";
  markerLabel.value = "";
}

function resetFormMessages() {
  audioErr.value = "";
  audioSuccess.value = "";
}

function onFileChange(event) {
  audioFile.value = event.target.files?.[0] ?? null;
}

function thumbnailContentUrl() {
  if (!props.selectedThumb?.id) return "";
  return buildApiUrl(`/api/thumbnails/${props.selectedThumb.id}/content`);
}

function hasMarker(audio) {
  return (
      audio?.markerX !== null &&
      audio?.markerX !== undefined &&
      audio?.markerY !== null &&
      audio?.markerY !== undefined &&
      audio?.markerX !== "" &&
      audio?.markerY !== ""
  );
}

function formatMarker(value) {
  const num = Number(value);
  return Number.isFinite(num) ? num.toFixed(2) : "-";
}

function isAudioActive(audio) {
  return String(props.activeAudioId ?? "") === String(audio?.id ?? "");
}

const discussionAudio = computed(() => {
  if (!props.audios.length) return null;

  if (focusedDiscussionAudioId.value != null) {
    const focused = props.audios.find(
        (audio) => String(audio.id) === String(focusedDiscussionAudioId.value)
    );
    if (focused) return focused;
  }

  if (props.activeAudioId != null) {
    const active = props.audios.find(
        (audio) => String(audio.id) === String(props.activeAudioId)
    );
    if (active) return active;
  }

  return props.audios[0] ?? null;
});

const markerPreviewStyle = computed(() => {
  if (markerX.value === "" || markerY.value === "") return null;
  return {
    left: `${markerX.value}%`,
    top: `${markerY.value}%`,
  };
});

async function ensureRecorder() {
  const stream = await navigator.mediaDevices.getUserMedia({audio: true});

  mediaRecorder = new MediaRecorder(stream);

  mediaRecorder.ondataavailable = (e) => {
    if (e.data && e.data.size > 0) {
      chunks.push(e.data);
    }
  };

  mediaRecorder.onstop = () => {
    recordedBlob = new Blob(chunks, {
      type: mediaRecorder.mimeType || "audio/webm",
    });
    chunks = [];
    isRecording.value = false;
  };
}

async function startRecording() {
  resetFormMessages();
  recordedBlob = null;

  try {
    if (!mediaRecorder) {
      await ensureRecorder();
    }

    chunks = [];
    mediaRecorder.start();
    isRecording.value = true;
  } catch (e) {
    audioErr.value = e.message || "Unable to start recording.";
    toast.error(audioErr.value);
  }
}

function stopRecording() {
  if (mediaRecorder && mediaRecorder.state !== "inactive") {
    mediaRecorder.stop();
  }
}

function appendMarker(fd) {
  if (markerX.value !== "" && markerY.value !== "") {
    fd.append("markerX", String(Number(markerX.value)));
    fd.append("markerY", String(Number(markerY.value)));
    fd.append("markerLabel", markerLabel.value || "");
  }
}

async function uploadRecording() {
  resetFormMessages();

  try {
    if (!props.selectedThumb) throw new Error("No thumbnail selected.");
    if (!recordedBlob) throw new Error("No recording available.");

    const fd = new FormData();
    fd.append("title", audioTitle.value || "");
    fd.append("audio", recordedBlob, "recording.webm");
    appendMarker(fd);

    await uploadThumbnailAudio(props.selectedThumb.id, fd);

    audioTitle.value = "";
    clearMarker();
    recordedBlob = null;
    audioSuccess.value = "Recording uploaded successfully.";
    toast.success(audioSuccess.value);
    emit("uploaded");
  } catch (e) {
    audioErr.value = e.message;
    toast.error(audioErr.value);
  }
}

async function uploadExistingAudio() {
  resetFormMessages();

  try {
    if (!props.selectedThumb) throw new Error("No thumbnail selected.");
    if (!audioFile.value) throw new Error("Please choose an audio file.");

    const fd = new FormData();
    fd.append("title", audioTitle.value || "");
    fd.append("audio", audioFile.value, audioFile.value.name);
    appendMarker(fd);

    await uploadThumbnailAudio(props.selectedThumb.id, fd);

    audioTitle.value = "";
    audioFile.value = null;
    clearMarker();
    audioSuccess.value = "Audio file uploaded successfully.";
    toast.success(audioSuccess.value);
    emit("uploaded");
  } catch (e) {
    audioErr.value = e.message;
    toast.error(audioErr.value);
  }
}

function onImageClick(event) {
  const rect = event.target.getBoundingClientRect();
  const x = ((event.clientX - rect.left) / rect.width) * 100;
  const y = ((event.clientY - rect.top) / rect.height) * 100;
  markerX.value = Math.max(0, Math.min(100, x)).toFixed(2);
  markerY.value = Math.max(0, Math.min(100, y)).toFixed(2);
}

function playAudio(audio) {
  emit("play-audio", audio);
}

function openDiscussion(audio) {
  focusedDiscussionAudioId.value = audio?.id ?? null;
}

function beginMarkerEdit(audio) {
  markerEditAudioId.value = audio.id;
  markerX.value = audio?.markerX != null ? Number(audio.markerX).toFixed(2) : "";
  markerY.value = audio?.markerY != null ? Number(audio.markerY).toFixed(2) : "";
  markerLabel.value = audio?.markerLabel ?? "";
  markerEditorOpen.value = true;
}

function cancelMarkerEdit() {
  markerEditAudioId.value = null;
  markerEditorOpen.value = false;
  clearMarker();
}

async function saveMarkerEdit() {
  resetFormMessages();

  try {
    if (!markerEditAudioId.value) {
      throw new Error("No audio selected for marker editing.");
    }

    const payload =
        markerX.value === "" || markerY.value === ""
            ? {markerX: null, markerY: null, markerLabel: null}
            : {
              markerX: Number(markerX.value),
              markerY: Number(markerY.value),
              markerLabel: markerLabel.value || null,
            };

    await updateAudioMarker(markerEditAudioId.value, payload);

    audioSuccess.value = "Marker updated successfully.";
    toast.success(audioSuccess.value);
    emit("uploaded");
    cancelMarkerEdit();
  } catch (e) {
    audioErr.value = e.message || "Failed to update marker.";
    toast.error(audioErr.value);
  }
}

async function removeMarker(audio) {
  resetFormMessages();

  try {
    await updateAudioMarker(audio.id, {
      markerX: null,
      markerY: null,
      markerLabel: null,
    });

    audioSuccess.value = "Marker removed successfully.";
    toast.success(audioSuccess.value);
    emit("uploaded");
    if (String(markerEditAudioId.value) === String(audio.id)) {
      cancelMarkerEdit();
    }
  } catch (e) {
    audioErr.value = e.message || "Failed to remove marker.";
    toast.error(audioErr.value);
  }
}

watch(
    () => props.selectedThumb,
    () => {
      resetFormMessages();
      clearMarker();
      audioTitle.value = "";
      audioFile.value = null;
      recordedBlob = null;
      isRecording.value = false;
      focusedDiscussionAudioId.value = null;
      markerEditAudioId.value = null;
      markerEditorOpen.value = false;
    }
);

watch(
    () => props.audios,
    (audios) => {
      if (!audios.length) {
        focusedDiscussionAudioId.value = null;
        return;
      }

      if (
          focusedDiscussionAudioId.value != null &&
          !audios.some((audio) => String(audio.id) === String(focusedDiscussionAudioId.value))
      ) {
        focusedDiscussionAudioId.value = null;
      }
    },
    {deep: true}
);
</script>

<template>
  <section v-if="selectedThumb" class="card audio-panel collapsible-card">
    <button
        type="button"
        class="collapsible-card__header"
        @click="togglePanel"
    >
      <div class="collapsible-card__title-block">
        <h2 class="collapsible-card__title">Audio workspace</h2>
        <p class="muted collapsible-card__summary">
          Thumbnail #{{ selectedThumb.idx ?? selectedThumb.id }}
          · {{ audios.length }} clip(s)
          <template v-if="activeAudioId">
            · current: {{ activeAudioTitle || `Audio #${activeAudioId}` }}
          </template>
        </p>
      </div>

      <div class="collapsible-card__header-right">
        <BaseBadge variant="info">
          {{ audios.length }} clip(s)
        </BaseBadge>

        <span class="collapsible-card__chevron" :class="{ 'is-open': panelOpen }">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round"
               stroke-linejoin="round">
            <path d="m6 9 6 6 6-6"></path>
          </svg>
        </span>
      </div>
    </button>

    <div v-if="panelOpen" class="collapsible-card__body">
      <section class="audio-panel__section">
        <h3>Scenario player</h3>

        <div class="scenario-player-card">
          <p class="muted scenario-player-card__text">
            <template v-if="activeAudioId">
              Current audio:
              <strong>{{ activeAudioTitle || `Audio #${activeAudioId}` }}</strong>
              ·
              <strong>{{ playerState }}</strong>
            </template>
            <template v-else>
              No audio currently selected.
            </template>
          </p>
        </div>
      </section>

      <section class="audio-panel__section">
        <h3>Existing audio clips</h3>
        <p class="muted">
          Click on a clip to play it through the single scenario player.
        </p>

        <div v-if="audios.length" class="audio-list">
          <article
              v-for="audio in audios"
              :key="audio.id"
              class="audio-item"
              :class="{ 'audio-item--active': isAudioActive(audio) }"
          >
            <div class="audio-item__header">
              <div>
                <h4 class="audio-item__title">
                  {{ audio.title || `Audio #${audio.id}` }}
                </h4>

                <p class="muted audio-item__meta">
                  <span v-if="audio.idx != null">
                    Order: <strong>{{ audio.idx }}</strong>
                  </span>

                  <template v-if="audio.markerLabel">
                    <span v-if="audio.idx != null"> · </span>
                    Marker label: <strong>{{ audio.markerLabel }}</strong>
                  </template>

                  <template v-if="hasMarker(audio)">
                    <span v-if="audio.idx != null || audio.markerLabel"> · </span>
                    Marker:
                    <strong>{{ formatMarker(audio.markerX) }}%</strong>,
                    <strong>{{ formatMarker(audio.markerY) }}%</strong>
                  </template>
                </p>
              </div>

              <div class="audio-item__actions">
                <BaseBadge v-if="isAudioActive(audio)" variant="warning">
                  Active
                </BaseBadge>

                <button
                    type="button"
                    class="btn btn--ghost"
                    @click="openDiscussion(audio)"
                >
                  Discuss
                </button>

                <button
                    type="button"
                    class="btn btn--primary"
                    @click="playAudio(audio)"
                >
                  Play
                </button>

                <button
                    v-if="isOwner"
                    type="button"
                    class="btn btn--ghost"
                    @click="beginMarkerEdit(audio)"
                >
                  Edit marker
                </button>

                <button
                    v-if="isOwner && hasMarker(audio)"
                    type="button"
                    class="btn btn--ghost"
                    @click="removeMarker(audio)"
                >
                  Remove marker
                </button>
              </div>
            </div>
          </article>
        </div>

        <p v-else class="muted">
          No audio clips are attached to this thumbnail yet.
        </p>
      </section>

      <section v-if="discussionAudio" class="audio-panel__section">
        <DiscussionThread
            title="Audio discussion"
            :subtitle="`Comments and annotation debate for ${discussionAudio.title || `Audio #${discussionAudio.id}`}.`"
            target-type="AUDIO"
            :target-id="discussionAudio.id"
            empty-title="No comments on this clip yet"
            empty-message="Use this space for transcription, translation, gloss or interpretation notes."
        />
      </section>

      <template v-if="isOwner">
        <div class="audio-panel__grid">
          <section class="audio-panel__section">
            <h3>{{ markerEditorOpen ? "Edit marker" : "1. Marker placement" }}</h3>
            <p class="muted">
              Click on the image to place the audio marker.
            </p>

            <div class="marker-picker">
              <div class="marker-image-stage">
                <img
                    :src="thumbnailContentUrl()"
                    alt="Marker picker"
                    class="marker-image"
                    @click="onImageClick"
                />

                <div
                    v-if="markerPreviewStyle"
                    class="marker-dot marker-dot--draft"
                    :style="markerPreviewStyle"
                    title="Selected marker position"
                ></div>
              </div>
            </div>

            <p class="muted">
              Marker:
              <strong>{{ markerX || "-" }}%</strong>,
              <strong>{{ markerY || "-" }}%</strong>
            </p>

            <label>
              Marker label
              <input v-model="markerLabel" placeholder="Optional marker label"/>
            </label>

            <div class="audio-panel__actions">
              <button type="button" class="btn btn--ghost" @click="clearMarker">
                Clear marker draft
              </button>

              <button
                  v-if="markerEditorOpen"
                  type="button"
                  class="btn btn--primary"
                  @click="saveMarkerEdit"
              >
                Save marker changes
              </button>

              <button
                  v-if="markerEditorOpen"
                  type="button"
                  class="btn btn--ghost"
                  @click="cancelMarkerEdit"
              >
                Cancel edit
              </button>
            </div>
          </section>

          <section class="audio-panel__section">
            <h3>2. Audio upload</h3>

            <label>
              Audio title
              <input v-model="audioTitle" placeholder="Optional title"/>
            </label>

            <div class="audio-panel__actions">
              <button
                  type="button"
                  class="btn btn--primary"
                  :disabled="isRecording"
                  @click="startRecording"
              >
                Start recording
              </button>

              <button
                  type="button"
                  class="btn btn--ghost"
                  :disabled="!isRecording"
                  @click="stopRecording"
              >
                Stop
              </button>

              <button
                  type="button"
                  class="btn btn--primary"
                  @click="uploadRecording"
              >
                Upload recording
              </button>
            </div>

            <p class="muted">
              Status:
              <strong>{{ isRecording ? "Recording..." : "Idle" }}</strong>
            </p>

            <div class="audio-panel__divider"></div>

            <label>
              Upload existing audio
              <input type="file" accept="audio/*" @change="onFileChange"/>
            </label>

            <button
                type="button"
                class="btn btn--primary"
                @click="uploadExistingAudio"
            >
              Upload audio file
            </button>

            <p v-if="audioSuccess" class="success">{{ audioSuccess }}</p>
            <p v-if="audioErr" class="error">{{ audioErr }}</p>
          </section>
        </div>
      </template>

      <template v-else>
        <p class="muted">
          You can view this scenario, but only the owner can upload audio and place markers.
        </p>
      </template>
    </div>
  </section>
</template>