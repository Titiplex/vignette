import {apiFetch} from "../api/rest.js";
import {el} from "./scenario.js"

export let selectedThumbId = null;
let selectedAudioForMarkerEdit = null;

export function setSelectedThumbId(id) {
    selectedThumbId = id;
    selectedAudioForMarkerEdit = null;
    resetMarkerForm();
    setMarkerEditState();
}

let recordedBlob = null;
let mediaRecorder = null;
let chunks = [];
let currentAudioList = [];

function sortByIdx(list) {
    return [...list].sort((a, b) => (a.idx ?? 0) - (b.idx ?? 0));
}

export async function loadAudiosForThumb(thumbId) {
    const list = await apiFetch(`/api/thumbnails/${thumbId}/audios`);
    const sorted = sortByIdx(list)
    currentAudioList = sorted;
    renderAudioList(sorted);
    return sorted;
}

function setMarkerEditState() {
    const label = el("audioMarkerEditState");
    if (!label) return;

    if (!selectedAudioForMarkerEdit) {
        label.textContent = "Marker mode: new upload";
        return;
    }

    label.textContent = `Marker mode: edit #${selectedAudioForMarkerEdit.idx} — ${selectedAudioForMarkerEdit.title}`;
}

function renderAudioList(list) {
    const box = el("audioList");
    if (!box) return;
    box.innerHTML = "";

    if (!list.length) {
        box.textContent = "No audio yet.";
        return;
    }

    for (const a of list) {
        const row = document.createElement("div");
        row.className = "card";
        row.style.marginBottom = "12px";

        const title = document.createElement("div");
        title.textContent = `#${a.idx} — ${a.title}`;

        const markerInfo = document.createElement("div");
        markerInfo.className = "audio-marker-summary";
        if (a.markerX != null && a.markerY != null) {
            markerInfo.textContent = `Marker: ${a.markerLabel || "untitled"} (${a.markerX.toFixed(1)}%, ${a.markerY.toFixed(1)}%)`;
        } else {
            markerInfo.textContent = "Marker: none";
        }

        const editBtn = document.createElement("button");
        editBtn.type = "button";
        editBtn.textContent = "Edit marker";
        editBtn.className = "secondary-btn";
        editBtn.addEventListener("click", () => selectAudioForMarkerEdit(a));

        const audio = document.createElement("audio");
        audio.controls = true;
        audio.src = `/api/audios/${a.id}/content`;

        row.appendChild(title);
        row.appendChild(markerInfo);
        row.appendChild(editBtn);
        row.appendChild(audio);
        box.appendChild(row);
    }
}

function updateMarkerCoordFields(x, y) {
    el("audioMarkerX").value = x.toFixed(2);
    el("audioMarkerY").value = y.toFixed(2);
    el("audioMarkerHint").textContent = `Marker position: ${x.toFixed(1)}%, ${y.toFixed(1)}%`;
}

function resetMarkerForm() {
    if (!el("audioMarkerX") || !el("audioMarkerY")) return;
    el("audioMarkerX").value = "";
    el("audioMarkerY").value = "";
    el("audioMarkerLabel").value = "";
    el("audioMarkerHint").textContent = "Click on the image to place a marker.";
    const dot = el("audioMarkerPreviewDot");
    if (dot) dot.style.display = "none";
}

function movePreviewDot(x, y) {
    const dot = el("audioMarkerPreviewDot");
    if (!dot) return;
    dot.style.display = "block";
    dot.style.left = `${x}%`;
    dot.style.top = `${y}%`;
}

function selectAudioForMarkerEdit(audio) {
    selectedAudioForMarkerEdit = audio;
    setMarkerEditState();

    if (audio.markerX != null && audio.markerY != null) {
        updateMarkerCoordFields(audio.markerX, audio.markerY);
        movePreviewDot(audio.markerX, audio.markerY);
    } else {
        resetMarkerForm();
    }

    el("audioMarkerLabel").value = audio.markerLabel || "";
}

function bindMarkerPicker() {
    const image = el("audioMarkerImage");
    if (!image) return;

    image.addEventListener("click", (event) => {
        const rect = image.getBoundingClientRect();
        const x = ((event.clientX - rect.left) / rect.width) * 100;
        const y = ((event.clientY - rect.top) / rect.height) * 100;

        const clampedX = Math.max(0, Math.min(100, x));
        const clampedY = Math.max(0, Math.min(100, y));
        updateMarkerCoordFields(clampedX, clampedY);
        movePreviewDot(clampedX, clampedY);
    });

    el("audioClearMarker").addEventListener("click", () => {
        resetMarkerForm();
    });
}

function readMarkerInputs(allowEmpty = false) {
    const markerX = el("audioMarkerX").value.trim();
    const markerY = el("audioMarkerY").value.trim();
    const markerLabel = el("audioMarkerLabel").value.trim();

    if (!markerX && !markerY) {
        if (allowEmpty) {
            return {markerX: null, markerY: null, markerLabel};
        }
        return null;
    }

    if (!markerX || !markerY) {
        throw new Error("Marker coordinates are incomplete");
    }

    return {
        markerX: Number(markerX),
        markerY: Number(markerY),
        markerLabel
    };
}

function appendMarkerToFormData(formData) {
    const marker = readMarkerInputs(false);
    if (!marker) return;

    formData.append("markerX", String(marker.markerX));
    formData.append("markerY", String(marker.markerY));
    formData.append("markerLabel", marker.markerLabel);
}

async function initRecorder() {
    const stream = await navigator.mediaDevices.getUserMedia({audio: true});

    const preferred = [
        "audio/webm;codecs=opus",
        "audio/webm",
        "audio/ogg;codecs=opus",
        "audio/ogg"
    ];
    let mimeType = "";
    for (const mt of preferred) {
        if (MediaRecorder.isTypeSupported(mt)) {
            mimeType = mt;
            break;
        }
    }

    mediaRecorder = new MediaRecorder(stream, mimeType ? {mimeType} : undefined);

    mediaRecorder.ondataavailable = (e) => {
        if (e.data && e.data.size > 0) chunks.push(e.data);
    };

    mediaRecorder.onstop = () => {
        recordedBlob = new Blob(chunks, {type: mediaRecorder.mimeType || "audio/webm"});
        chunks = [];
        el("uploadAudio").disabled = false;
        el("recStart").disabled = false;
        el("recStop").disabled = true;
    };
}

async function startRecording() {
    el("audioErr").textContent = "";
    recordedBlob = null;
    el("uploadAudio").disabled = true;

    if (!mediaRecorder) await initRecorder();

    chunks = [];
    mediaRecorder.start();
    el("recStart").disabled = true;
    el("recStop").disabled = false;
}

function stopRecording() {
    if (mediaRecorder && mediaRecorder.state !== "inactive") {
        mediaRecorder.stop();
    }
}

async function uploadRecording() {
    if (!selectedThumbId) throw new Error("No thumbnail selected");
    if (!recordedBlob) throw new Error("No recording available");

    const fd = new FormData();
    fd.append("title", el("audioTitle").value || "");
    fd.append("audio", recordedBlob, "recording.webm");
    appendMarkerToFormData(fd);

    await apiFetch(`/api/thumbnails/${selectedThumbId}/audios`, {
        method: "POST",
        body: fd
    });

    el("audioTitle").value = "";
    recordedBlob = null;
    el("uploadAudio").disabled = true;

    await loadAudiosForThumb(selectedThumbId);
    selectedAudioForMarkerEdit = null;
    resetMarkerForm();
    setMarkerEditState();
    window.dispatchEvent(new CustomEvent("audio-uploaded"));
}

export async function uploadAudioFile() {
    if (!selectedThumbId) throw new Error("No thumbnail selected");

    const audioFile = el("audioFile").files?.[0];
    if (!audioFile) throw new Error("Please choose an audio file");

    const fd = new FormData();
    fd.append("title", el("audioTitle").value || "");
    fd.append("audio", audioFile, audioFile.name);
    appendMarkerToFormData(fd);

    await apiFetch(`/api/thumbnails/${selectedThumbId}/audios`, {
        method: "POST",
        body: fd
    });

    el("audioFile").value = "";
    await loadAudiosForThumb(selectedThumbId);
    selectedAudioForMarkerEdit = null;
    resetMarkerForm();
    setMarkerEditState();
    window.dispatchEvent(new CustomEvent("audio-uploaded"));
}

async function saveMarkerForExistingAudio() {
    if (!selectedAudioForMarkerEdit) {
        throw new Error("Select an existing audio first via 'Edit marker'");
    }

    const marker = readMarkerInputs(true);

    await apiFetch(`/api/audios/${selectedAudioForMarkerEdit.id}/marker`, {
        method: "PATCH",
        body: marker
    });

    await loadAudiosForThumb(selectedThumbId);
    const updatedAudio = currentAudioList.find(a => a.id === selectedAudioForMarkerEdit.id);
    selectedAudioForMarkerEdit = updatedAudio || selectedAudioForMarkerEdit;
    setMarkerEditState();
    window.dispatchEvent(new CustomEvent("audio-uploaded"));
}

function updateMarkerPreviewImage() {
    const image = el("audioMarkerImage");
    if (!image || !selectedThumbId) return;
    image.src = `/api/thumbnails/${selectedThumbId}/content`;
}

el("recStart").addEventListener("click", () => startRecording().catch(e => el("audioErr").textContent = e.message));
el("recStop").addEventListener("click", () => stopRecording());
el("uploadAudio").addEventListener("click", () => uploadRecording().catch(e => el("audioErr").textContent = e.message));
el("saveMarkerForExistingAudio").addEventListener("click", () => saveMarkerForExistingAudio().catch(e => el("audioErr").textContent = e.message));

bindMarkerPicker();
window.addEventListener("thumbnail-selected", () => {
    updateMarkerPreviewImage();
    setMarkerEditState();
});
window.addEventListener("audio-uploaded", updateMarkerPreviewImage);
