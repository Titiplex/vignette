import {apiFetch, setAccessToken} from "../api/rest.js";
import {updateHeaderAuth} from "../api/header.js";

updateHeaderAuth().then(() => {});

async function loadMe() {
    return apiFetch("/api/auth/me");
}

// restore token si refresh
const t = sessionStorage.getItem("accessToken");
if (t) setAccessToken(t);

try {
    await loadMe();
} catch (_) {
    window.location.href = "/pages/login.html";
}

const select = document.getElementById("langSelect");
const filter = document.getElementById("langFilter");
const prevBtn = document.getElementById("langPrev");
const nextBtn = document.getElementById("langNext");
const pageInfo = document.getElementById("langPageInfo");
const form = document.getElementById("scenarioForm");
const error = document.getElementById("error");

const PAGE_SIZE = 50;
let currentPage = 0;
let currentQuery = "";
let totalPages = 0;

function renderOptions(options = []) {
    select.innerHTML = `<option value="" disabled selected>-- choose --</option>`;
    for (const l of options) {
        const opt = document.createElement("option");
        opt.value = l.id;
        opt.textContent = l.name;
        select.appendChild(opt);
    }
}

async function loadLanguages(page = 0, q = "") {
    const params = new URLSearchParams({
        page: String(page),
        size: String(PAGE_SIZE),
    });

    if (q.trim()) {
        params.set("q", q.trim());
    }

    const data = await apiFetch(`/api/languages/options?${params.toString()}`);
    currentPage = data.number ?? page;
    totalPages = data.totalPages ?? 0;
    renderOptions(data.content ?? []);
    pageInfo.textContent = totalPages > 0 ? `Page ${currentPage + 1}/${totalPages}` : "No result";
    prevBtn.disabled = currentPage <= 0;
    nextBtn.disabled = totalPages === 0 || currentPage >= totalPages - 1;
}

filter.addEventListener("input", () => {
    currentQuery = filter.value;
    loadLanguages(0, currentQuery).catch(e => error.textContent = e.message);
});

prevBtn.addEventListener("click", () => {
    if (currentPage <= 0) return;
    loadLanguages(currentPage - 1, currentQuery).catch(e => error.textContent = e.message);
});

nextBtn.addEventListener("click", () => {
    if (totalPages === 0 || currentPage >= totalPages - 1) return;
    loadLanguages(currentPage + 1, currentQuery).catch(e => error.textContent = e.message);
});

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    error.textContent = "";

    const body = {
        title: form.title.value.trim(),
        description: form.description.value,
        languageId: form.languageId.value,
    };

    try {
        const created = await apiFetch("/api/scenarios", {method: "POST", body});
        // suppose: {id: 123}
        window.location.href = `/pages/scenario.html?id=${created.id}`;
    } catch (err) {
        error.textContent = err.message;
        if ((err.message || "").includes("401")) {
            window.location.href = "/pages/login.html";
        }
    }
});

loadLanguages(0, "").catch(e => error.textContent = e.message);
