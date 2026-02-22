import "../style.css";
import {apiFetch, setAccessToken} from "../api/rest.js";
import {loadAudiosForThumb, setSelectedThumbId, uploadAudioFile} from "./audio.js"
import {updateHeaderAuth} from "../api/header.js";

updateHeaderAuth().then(() => {
});

const t = sessionStorage.getItem("accessToken");
let isOwner = false;
if (t) setAccessToken(t);

function qp(name) {
    const u = new URL(window.location.href);
    return u.searchParams.get(name) ?? "";
}

export function el(id) {
    return document.getElementById(id);
}

async function loadScenario(id) {
    return apiFetch(`/api/scenarios/${encodeURIComponent(id)}`);
}

async function loadMe() {
    return apiFetch("/api/auth/me");
}

async function loadThumbnails(scenarioId) {
    return apiFetch(`/api/scenarios/${encodeURIComponent(scenarioId)}/thumbnails`);
}

function createAudioItem(audio) {
    const row = document.createElement("div");
    row.className = "thumb-audio-item";

    const title = document.createElement("div");
    title.className = "thumb-audio-title";
    title.textContent = `#${audio.idx} — ${audio.title || "Untitled audio"}`;

    const player = document.createElement("audio");
    player.controls = true;
    player.src = `/api/audios/${audio.id}/content`;

    row.appendChild(title);
    row.appendChild(player);
    return row;
}

async function renderThumbs(list) {
    const grid = el("thumbGrid");
    grid.innerHTML = "";

    if (!list.length) {
        grid.textContent = "No images yet.";
        return;
    }

    const sorted = [...list].sort((a, b) => (a.idx ?? 0) - (b.idx ?? 0));

    for (const t of sorted) {
        const wrap = document.createElement("div");
        wrap.className = "card thumb-card";
        wrap.style.marginBottom = "12px";

        wrap.onclick = async () => {
            setSelectedThumbId(t.id);
            el("audioCard").style.display = "block";
            el("selectedThumb").textContent = `#${t.idx} (id=${t.id})`;
            await loadAudiosForThumb(t.id);
            el("audioOwnerTools").style.display = isOwner ? "block" : "none";
        };

        const img = document.createElement("img");
        img.src = `/api/thumbnails/${t.id}/content`;
        img.alt = t.title || "";
        img.style.maxWidth = "100%";

        const cap = document.createElement("div");
        cap.className = "caption";
        const idxLabel = (t.idx ?? "?");
        cap.textContent = `#${idxLabel} — ${t.title || `thumb ${t.id}`}`;

        const audioList = document.createElement("div");
        audioList.className = "thumb-audio-list";
        audioList.textContent = "Loading audios...";

        wrap.appendChild(img);
        wrap.appendChild(cap);
        wrap.appendChild(audioList);
        grid.appendChild(wrap);

        try {
            const audios = await loadAudiosForThumb(t.id);
            audioList.innerHTML = "";
            if (!audios.length) {
                audioList.textContent = "No audio yet.";
            } else {
                for (const a of audios) {
                    audioList.appendChild(createAudioItem(a));
                }
            }
        } catch (e) {
            audioList.textContent = `Unable to load audio: ${e.message}`;
        }
    }
}

function getLang(id) {
    return apiFetch(`/api/languages/${id}`);
}

async function main() {
    const id = qp("id");
    if (!id) throw new Error("Missing scenario id");

    const s = await loadScenario(id);

    el("scenarioId").value = String(s.id);

    el("title").textContent = s.title ?? "Scenario";

    if (s.languageId) getLang(s.languageId).then(lang => el("lang").textContent = lang.name ?? "Unknown language")

    el("author").textContent = s.authorUsername ?? "";
    el("desc").textContent = s.description ?? "";

    try {
        const me = await loadMe();
        isOwner = me.username === s.authorUsername;

        if (isOwner) {
            el("uploadCard").style.display = "block";
        }
    } catch (_) {
        isOwner = false;
        el("uploadCard").style.display = "none";
    }

    const thumbs = await loadThumbnails(s.id);
    await renderThumbs(thumbs);

    const form = el("uploadForm");
    const uploadError = el("uploadError");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        uploadError.textContent = "";

        const fd = new FormData();
        fd.append("scenarioId", el("scenarioId").value);
        fd.append("title", form.title.value || "");
        fd.append("image", form.image.files[0]);

        try {
            await apiFetch("/api/thumbnails", {method: "POST", body: fd});
            const updated = await loadThumbnails(s.id);
            await renderThumbs(updated);
            form.reset();
        } catch (err) {
            uploadError.textContent = err.message;
            if ((err.message || "").includes("401") || (err.message || "").includes("403")) {
                window.location.href = "/pages/login.html";
            }
        }
    });


    window.addEventListener("audio-uploaded", async () => {
        const updated = await loadThumbnails(s.id);
        await renderThumbs(updated);
    });

    el("uploadExistingAudio").addEventListener("click", async () => {
        el("audioErr").textContent = "";
        try {
            await uploadAudioFile();
        } catch (err) {
            el("audioErr").textContent = err.message;
        }
    });
}

main().catch(e => {
    el("error").textContent = e.message;
});
