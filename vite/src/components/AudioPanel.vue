<script setup>
import {ref, watch} from "vue";
import {uploadThumbnailAudio} from "../api/scenarios";
import {buildApiUrl} from "../api/rest";
import {useToast} from "../composables/useToast";

const props = defineProps({
  selectedThumb: {type: Object, default: null},
  audios: {type: Array, default: () => []},
  isOwner: {type: Boolean, default: false},
});

const emit = defineEmits(["uploaded"]);
const toast = useToast();

const audioTitle = ref("");
const audioFile = ref(null);
const audioErr = ref("");
const audioSuccess = ref("");
const markerX = ref("");
const markerY = ref("");
const markerLabel = ref("");
const isRecording = ref(false);

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

function audioContentUrl(audio) {
  if (!audio?.id) return "";
  return buildApiUrl(`/api/audios/${audio.id}/content`);
}

function formatMarker(value) {
  const num = Number(value);
  return Number.isFinite(num) ? num.toFixed(2) : "-";
}

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

watch(
    () => props.selectedThumb,
    () => {
      resetFormMessages();
      clearMarker();
      audioTitle.value = "";
      audioFile.value = null;
      recordedBlob = null;
      isRecording.value = false;
    }
);
</script>

<template>
  <section v-if="selectedThumb" class="card audio-panel">
    <div class="section-heading">
      <div>
        <h2>Audio workspace</h2>
        <p class="muted">
          Selected thumbnail #{{ selectedThumb.idx ?? selectedThumb.id }}
        </p>
      </div>
    </div>

    <section class="audio-panel__section">
      <h3>Existing audio clips</h3>
      <p class="muted">
        Listen to the audio already attached to this thumbnail.
      </p>

      <div v-if="audios.length" class="audio-list">
        <article v-for="audio in audios" :key="audio.id" class="audio-item">
          <div class="audio-item__header">
            <div>
              <h4 class="audio-item__title">
                {{ audio.title || `Audio #${audio.id}` }}
              </h4>

              <p class="muted audio-item__meta">
                <template v-if="audio.markerLabel">
                  Marker label: <strong>{{ audio.markerLabel }}</strong>
                </template>
                <template v-if="audio.markerX != null && audio.markerY != null">
                  <span v-if="audio.markerLabel"> · </span>
                  Marker:
                  <strong>{{ formatMarker(audio.markerX) }}%</strong>,
                  <strong>{{ formatMarker(audio.markerY) }}%</strong>
                </template>
              </p>
            </div>
          </div>

          <audio class="audio-player" controls preload="none">
            <source :src="audioContentUrl(audio)"/>
            Your browser does not support audio playback.
          </audio>
        </article>
      </div>

      <p v-else class="muted">
        No audio clips are attached to this thumbnail yet.
      </p>
    </section>

    <template v-if="isOwner">
      <div class="audio-panel__grid">
        <section class="audio-panel__section">
          <h3>1. Marker placement</h3>
          <p class="muted">
            Click on the image to place the audio marker.
          </p>

          <div class="marker-picker">
            <img
                :src="thumbnailContentUrl()"
                alt="Marker picker"
                class="marker-image"
                @click="onImageClick"
            />
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

          <button type="button" class="btn btn--ghost" @click="clearMarker">
            Clear marker
          </button>
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
  </section>
</template>