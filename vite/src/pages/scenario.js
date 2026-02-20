import "../style.css";
import {apiFetch, setAccessToken} from "../api/rest.js";
import {loadAudiosForThumb, setSelectedThumbId} from "./audio.js"
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

function renderThumbs(list) {
    const grid = el("thumbGrid");
    grid.innerHTML = "";

    if (!list.length) {
        grid.textContent = "No images yet.";
        return;
    }

    // security order sort
    const sorted = [...list].sort((a, b) => (a.idx ?? 0) - (b.idx ?? 0));

    for (const t of sorted) {
        const wrap = document.createElement("div");
        wrap.className = "card";
        wrap.classList.add("thumb-card");
        wrap.style.marginBottom = "12px";

        wrap.style.cursor = "pointer";
        wrap.onclick = async () => {
            setSelectedThumbId(t.id);
            el("audioCard").style.display = "block";
            el("selectedThumb").textContent = `#${t.idx} (id=${t.id})`;

            await loadAudiosForThumb(t.id);

            // montre outils si owner
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

        wrap.appendChild(img);
        wrap.appendChild(cap);
        grid.appendChild(wrap);
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
        // window.location.href = "/pages/login.html";
        isOwner = false;
        el("uploadCard").style.display = "none";
    }

    const thumbs = await loadThumbnails(s.id);
    renderThumbs(thumbs);

    // handler upload
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
            // refresh list
            const updated = await loadThumbnails(s.id);
            renderThumbs(updated);
            form.reset();
        } catch (err) {
            uploadError.textContent = err.message;
            if ((err.message || "").includes("401") || (err.message || "").includes("403")) {
                window.location.href = "/pages/login.html";
            }
        }
    });
}

main().catch(e => {
    el("error").textContent = e.message;
});
