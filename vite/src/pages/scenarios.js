import "../style.css";
import {apiFetch} from "../api/rest.js";
import {updateHeaderAuth} from "../api/header.js";

updateHeaderAuth().then(() => {
});

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

function matchesScenario(scenario, searchTerm) {
    const q = searchTerm.toLowerCase();
    const title = (scenario.title ?? "").toLowerCase();
    const language = (scenario.languageId ?? "").toLowerCase();
    const author = (scenario.authorUsername ?? "").toLowerCase();
    return title.includes(q) || language.includes(q) || author.includes(q);
}

async function main() {
    const scenarios = await apiFetch("/api/scenarios");

    const info = document.getElementById("scenariosInfo");
    const grid = document.getElementById("scenariosGrid");
    const searchInput = document.getElementById("scenarioSearch");

    if (!Array.isArray(scenarios) || scenarios.length === 0) {
        info.textContent = "No scenarios available yet.";
        grid.innerHTML = "";
        return;
    }

    const previewMap = new Map();
    await Promise.all(scenarios.map(async (s) => {
        try {
            const thumbs = await apiFetch(`/api/scenarios/${encodeURIComponent(s.id)}/thumbnails`);
            previewMap.set(s.id, thumbs?.[0]?.id ?? null);
        } catch (_) {
            previewMap.set(s.id, null);
        }
    }));

    function render(searchTerm = "") {
        const filteredScenarios = searchTerm
            ? scenarios.filter((s) => matchesScenario(s, searchTerm))
            : scenarios;

        info.textContent = `${filteredScenarios.length} scenario(s) available`;

        grid.innerHTML = "";
        for (const s of filteredScenarios) {
            const article = document.createElement("article");
            article.className = "card thumb-card";
            article.innerHTML = cardHtml(s, previewMap.get(s.id));
            grid.appendChild(article);
        }
    }

    render();

    if (searchInput) {
        searchInput.addEventListener("input", () => render(searchInput.value.trim()));
    }
}

main().catch((e) => {
    const info = document.getElementById("scenariosInfo");
    if (info) info.textContent = e.message;
});
