<script setup>
import {ref, watch} from "vue";
import {apiFetch} from "../api/rest";

const props = defineProps({
  selectedThumb: {type: Object, default: null},
  isOwner: {type: Boolean, default: false},
});

const emit = defineEmits(["uploaded"]);

const audioTitle = ref("");
const audioFile = ref(null);
const audioErr = ref("");
const markerX = ref("");
const markerY = ref("");
const markerLabel = ref("");

let mediaRecorder = null;
let chunks = [];
let recordedBlob = null;

function clearMarker() {
  markerX.value = "";
  markerY.value = "";
  markerLabel.value = "";
}

function onFileChange(event) {
  audioFile.value = event.target.files?.[0] ?? null;
}

async function ensureRecorder() {
  const stream = await navigator.mediaDevices.getUserMedia({audio: true});
  mediaRecorder = new MediaRecorder(stream);
  mediaRecorder.ondataavailable = (e) => {
    if (e.data && e.data.size > 0) chunks.push(e.data);
  };
  mediaRecorder.onstop = () => {
    recordedBlob = new Blob(chunks, {type: mediaRecorder.mimeType || "audio/webm"});
    chunks = [];
  };
}

async function startRecording() {
  audioErr.value = "";
  recordedBlob = null;
  if (!mediaRecorder) {
    await ensureRecorder();
  }
  chunks = [];
  mediaRecorder.start();
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
    fd.append("markerLabel", markerLabel.value);
  }
}

async function uploadRecording() {
  if (!props.selectedThumb) throw new Error("No thumbnail selected");
  if (!recordedBlob) throw new Error("No recording available");

  const fd = new FormData();
  fd.append("title", audioTitle.value || "");
  fd.append("audio", recordedBlob, "recording.webm");
  appendMarker(fd);

  await apiFetch(`/api/thumbnails/${props.selectedThumb.id}/audios`, {
    method: "POST",
    body: fd,
  });

  audioTitle.value = "";
  clearMarker();
  emit("uploaded");
}

async function uploadExistingAudio() {
  if (!props.selectedThumb) throw new Error("No thumbnail selected");
  if (!audioFile.value) throw new Error("Please choose an audio file");

  const fd = new FormData();
  fd.append("title", audioTitle.value || "");
  fd.append("audio", audioFile.value, audioFile.value.name);
  appendMarker(fd);

  await apiFetch(`/api/thumbnails/${props.selectedThumb.id}/audios`, {
    method: "POST",
    body: fd,
  });

  audioTitle.value = "";
  audioFile.value = null;
  clearMarker();
  emit("uploaded");
}

function onImageClick(event) {
  const rect = event.target.getBoundingClientRect();
  const x = ((event.clientX - rect.left) / rect.width) * 100;
  const y = ((event.clientY - rect.top) / rect.height) * 100;
  markerX.value = Math.max(0, Math.min(100, x)).toFixed(2);
  markerY.value = Math.max(0, Math.min(100, y)).toFixed(2);
}

watch(() => props.selectedThumb, () => {
  audioErr.value = "";
  clearMarker();
});
</script>

<template>
  <section v-if="selectedThumb" class="card">
    <h2>Audio recordings</h2>
    <p>Selected thumbnail: #{{ selectedThumb.idx }} (id={{ selectedThumb.id }})</p>

    <template v-if="isOwner">
      <div class="marker-picker">
        <img
            :src="`/api/thumbnails/${selectedThumb.id}/content`"
            alt="Marker picker"
            class="marker-image"
            @click="onImageClick"
        />
      </div>

      <p>
        Marker position:
        {{ markerX || "-" }}%, {{ markerY || "-" }}%
      </p>

      <input v-model="markerLabel" placeholder="Marker label"/>

      <div class="toolbar">
        <button type="button" @click="startRecording">Start recording</button>
        <button type="button" @click="stopRecording">Stop</button>
        <button type="button" @click="uploadRecording">Upload recording</button>
      </div>

      <div class="toolbar">
        <input v-model="audioTitle" placeholder="Audio title"/>
        <input type="file" accept="audio/*" @change="onFileChange"/>
        <button type="button" @click="uploadExistingAudio">Upload audio file</button>
      </div>

      <button type="button" @click="clearMarker">Clear marker</button>
      <p v-if="audioErr" class="error">{{ audioErr }}</p>
    </template>
  </section>
</template>