import {apiFetch} from "../api/rest.js";
import {el} from "./scenario.js"

export let selectedThumbId = null;
let recordedBlob = null;
let mediaRecorder = null;
let chunks = [];

function sortByIdx(list) {
    return [...list].sort((a, b) => (a.idx ?? 0) - (b.idx ?? 0));
}

export async function loadAudiosForThumb(thumbId) {
    const list = await apiFetch(`/api/thumbnails/${thumbId}/audios`);
    renderAudioList(sortByIdx(list));
}

function renderAudioList(list) {
    const box = el("audioList");
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

        const audio = document.createElement("audio");
        audio.controls = true;
        audio.src = `/api/audios/${a.id}/content`;

        row.appendChild(title);
        row.appendChild(audio);
        box.appendChild(row);
    }
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

    await apiFetch(`/api/thumbnails/${selectedThumbId}/audios`, {
        method: "POST",
        body: fd
    });

    el("audioTitle").value = "";
    recordedBlob = null;
    el("uploadAudio").disabled = true;

    await loadAudiosForThumb(selectedThumbId);
}

el("recStart").addEventListener("click", () => startRecording().catch(e => el("audioErr").textContent = e.message));
el("recStop").addEventListener("click", () => stopRecording());
el("uploadAudio").addEventListener("click", () => uploadRecording().catch(e => el("audioErr").textContent = e.message));
