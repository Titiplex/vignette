import "../style.css";
import {apiFetch, setAccessToken} from "../api/rest.js";

const t = sessionStorage.getItem("accessToken");
if (t) setAccessToken(t);

function qp(name, def = null) {
    const u = new URL(window.location.href);
    return u.searchParams.get(name) ?? def;
}

function el(id) {
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
        wrap.style.marginBottom = "12px";

        const img = document.createElement("img");
        img.src = `/api/thumbnails/${t.id}/content`;
        img.alt = t.title || "";
        img.style.maxWidth = "100%";

        const cap = document.createElement("div");
        const idxLabel = (t.idx ?? "?");
        cap.textContent = `#${idxLabel} — ${t.title || `thumb ${t.id}`}`;

        wrap.appendChild(img);
        wrap.appendChild(cap);
        grid.appendChild(wrap);
    }
}

async function main() {
    const id = qp("id");
    if (!id) throw new Error("Missing scenario id");

    const s = await loadScenario(id);

    el("scenarioId").value = String(s.id);

    el("title").textContent = s.title ?? "Scenario";
    el("lang").textContent = s.languageId ?? "-";

    const authorUsername = s.author?.username ?? "-";
    el("author").textContent = authorUsername;
    el("desc").textContent = s.description ?? "";

    try {
        const me = await loadMe();
        const isOwner = me.username === authorUsername;

        if (isOwner) {
            el("uploadCard").style.display = "block";
        }
    } catch (_) {
        window.location.href = "/pages/login.html";
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