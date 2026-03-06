import "../style.css";
import {apiFetch} from "../api/rest.js";
import {updateHeaderAuth} from "../api/header.js";

updateHeaderAuth().then(() => {});

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function cardHtml(s, thumbId) {
    const preview = thumbId
        ? `<img src="/api/thumbnails/${thumbId}/content" alt="Preview for ${escapeHtml(s.title ?? "scenario")}" loading="lazy" />`
        : `<div class="text">No image preview yet.</div>`;

    return `
        ${preview}
        <h3>${escapeHtml(s.title ?? "Untitled scenario")}</h3>
        <p class="text">${escapeHtml(s.description ?? "No description")}</p>
        <p class="text"><strong>Language:</strong> ${escapeHtml(s.languageId ?? "-")}</p>
        <p class="text"><strong>Author:</strong> ${escapeHtml(s.authorUsername ?? "-")}</p>
        <a href="/pages/scenario.html?id=${encodeURIComponent(s.id)}">Open scenario</a>
        <div class="template-actions">
            <button type="button" class="secondary-btn publish-template-btn" data-scenario-id="${s.id}">
                Publish as template
            </button>
        </div>
    `;
}

function templateCardHtml(template) {
    return `
        <h3>${escapeHtml(template.title ?? "Untitled template")}</h3>
        <p class="text">${escapeHtml(template.description ?? "No description")}</p>
        <p class="text"><strong>Language:</strong> ${escapeHtml(template.languageId ?? "-")}</p>
        <p class="text"><strong>Source author:</strong> ${escapeHtml(template.sourceAuthor ?? "-")}</p>
        <div class="template-actions">
            <input type="text" class="instantiate-title" placeholder="Optional title" aria-label="Custom title"/>
            <button type="button" class="instantiate-btn" data-template-id="${template.id}">
                Use this template
            </button>
        </div>
    `;
}

async function getCurrentUsername() {
    try {
        const me = await apiFetch("/api/auth/me");
        return me?.username ?? null;
    } catch (_) {
        return null;
    }
}

async function loadScenarioCards() {
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

async function loadTemplateCards() {
    const templatesInfo = document.getElementById("templatesInfo");
    const templatesGrid = document.getElementById("templatesGrid");
    const languageFilter = document.getElementById("templateLanguageFilter");

    const params = new URLSearchParams();
    if (languageFilter.value.trim()) {
        params.set("languageId", languageFilter.value.trim());
    }

    const query = params.toString();
    const templates = await apiFetch(`/api/template-scenarios${query ? `?${query}` : ""}`);

    if (!Array.isArray(templates) || templates.length === 0) {
        templatesInfo.textContent = "No templates available for this filter.";
        templatesGrid.innerHTML = "";
        return;
    }

    templatesInfo.textContent = `${templates.length} template(s) available`;
    templatesGrid.innerHTML = "";

    for (const template of templates) {
        const article = document.createElement("article");
        article.className = "card thumb-card";
        article.innerHTML = templateCardHtml(template);
        templatesGrid.appendChild(article);
    }
}

async function publishScenarioAsTemplate(scenarioId) {
    await apiFetch(`/api/template-scenarios/${encodeURIComponent(scenarioId)}`, {
        method: "POST"
    });
}

async function instantiateTemplate(templateId, title) {
    const created = await apiFetch(`/api/template-scenarios/${encodeURIComponent(templateId)}/instantiate`, {
        method: "POST",
        body: title?.trim() ? {title: title.trim()} : {}
    });

    if (created?.id != null) {
        window.location.href = `/pages/scenario.html?id=${created.id}`;
    }
}

async function main() {
    const currentUsername = await getCurrentUsername();

    await loadTemplateCards();
    await loadScenarioCards();

    const scenariosGrid = document.getElementById("scenariosGrid");
    const templatesGrid = document.getElementById("templatesGrid");
    const templatesInfo = document.getElementById("templatesInfo");

    scenariosGrid.addEventListener("click", async (event) => {
        const button = event.target.closest(".publish-template-btn");
        if (!button) return;

        if (!currentUsername) {
            alert("You must be logged in to publish templates.");
            window.location.href = "/pages/login.html";
            return;
        }

        button.disabled = true;
        const scenarioId = button.dataset.scenarioId;

        try {
            await publishScenarioAsTemplate(scenarioId);
            templatesInfo.textContent = "Template published.";
            await loadTemplateCards();
        } catch (e) {
            templatesInfo.textContent = `Cannot publish template: ${e.message}`;
        } finally {
            button.disabled = false;
        }
    });

    templatesGrid.addEventListener("click", async (event) => {
        const button = event.target.closest(".instantiate-btn");
        if (!button) return;

        if (!currentUsername) {
            alert("You must be logged in to instantiate templates.");
            window.location.href = "/pages/login.html";
            return;
        }

        const card = button.closest("article");
        const titleInput = card?.querySelector(".instantiate-title");
        const title = titleInput?.value ?? "";

        button.disabled = true;
        try {
            await instantiateTemplate(button.dataset.templateId, title);
        } catch (e) {
            templatesInfo.textContent = `Cannot instantiate template: ${e.message}`;
            button.disabled = false;
        }
    });

    document.getElementById("templateRefreshBtn").addEventListener("click", () => {
        loadTemplateCards().catch((e) => {
            templatesInfo.textContent = e.message;
        });
    });

    document.getElementById("templateLanguageFilter").addEventListener("input", () => {
        loadTemplateCards().catch((e) => {
            templatesInfo.textContent = e.message;
        });
    });
}

main().catch((e) => {
    const info = document.getElementById("scenariosInfo");
    if (info) info.textContent = e.message;
});
