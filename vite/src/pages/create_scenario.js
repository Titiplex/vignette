import {apiFetch, setAccessToken} from "../api/rest.js";

// restore token si refresh
const t = sessionStorage.getItem("accessToken");
if (t) setAccessToken(t);

const select = document.getElementById("langSelect");
const filter = document.getElementById("langFilter");
const form = document.getElementById("scenarioForm");
const error = document.getElementById("error");

let allOptions = [];

function renderOptions(q = "") {
    const qq = q.toLowerCase();
    select.innerHTML = `<option value="" disabled selected>-- choose --</option>`;
    for (const l of allOptions) {
        if (!l.name.toLowerCase().includes(qq)) continue;
        const opt = document.createElement("option");
        opt.value = l.id;
        opt.textContent = l.name;
        select.appendChild(opt);
    }
}

async function loadLanguages() {
    // endpoint léger -> [{id,name}]
    allOptions = await apiFetch("/api/languages/options");
    renderOptions("");
}

filter.addEventListener("input", () => renderOptions(filter.value));

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
            window.location.href = "/front/pages/login.html";
        }
    }
});

loadLanguages().catch(e => error.textContent = e.message);
