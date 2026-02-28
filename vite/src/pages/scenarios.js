import "../style.css";
import {apiFetch} from "../api/rest.js";
import {updateHeaderAuth} from "../api/header.js";

updateHeaderAuth().then(() => {});

function cardHtml(s, thumbId) {
    const preview = thumbId
        ? `<img src="/api/thumbnails/${thumbId}/content" alt="Preview for ${s.title ?? "scenario"}" loading="lazy" />`
        : `<div class="text">No image preview yet.</div>`;

    return `
        ${preview}
        <h3>${s.title ?? "Untitled scenario"}</h3>
        <p class="text">${s.description ?? "No description"}</p>
        <p class="text"><strong>Language:</strong> ${s.languageId ?? "-"}</p>
        <p class="text"><strong>Author:</strong> ${s.authorUsername ?? "-"}</p>
        <a href="/pages/scenario.html?id=${encodeURIComponent(s.id)}">Open scenario</a>
    `;
}

async function main() {
    const scenarios = await apiFetch("/api/scenarios");

    const info = document.getElementById("scenariosInfo");
    const grid = document.getElementById("scenariosGrid");

    if (!Array.isArray(scenarios) || scenarios.length === 0) {
        info.textContent = "No scenarios available yet.";
        grid.innerHTML = "";
        return;
    }

    info.textContent = `${scenarios.length} scenario(s) available`;

    const previewMap = new Map();
    await Promise.all(scenarios.map(async (s) => {
        try {
            const thumbs = await apiFetch(`/api/scenarios/${encodeURIComponent(s.id)}/thumbnails`);
            previewMap.set(s.id, thumbs?.[0]?.id ?? null);
        } catch (_) {
            previewMap.set(s.id, null);
        }
    }));

    grid.innerHTML = "";
    for (const s of scenarios) {
        const article = document.createElement("article");
        article.className = "card thumb-card";
        article.innerHTML = cardHtml(s, previewMap.get(s.id));
        grid.appendChild(article);
    }
}

main().catch((e) => {
    const info = document.getElementById("scenariosInfo");
    if (info) info.textContent = e.message;
});