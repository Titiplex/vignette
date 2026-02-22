import "../style.css";
import {apiFetch} from "../api/rest.js";
import {updateHeaderAuth} from "../api/header.js";

updateHeaderAuth().then(() => {});

function qp(id) {
    const u = new URL(window.location.href);
    return u.searchParams.get(id) ?? "";
}

async function main() {
    const id = String(qp("id"));
    const data = await apiFetch(`/api/languages/${id}`);

    const name = document.getElementById("name");
    const family = document.getElementById("family");
    const parent = document.getElementById("parent");
    const level = document.getElementById("level");
    const desc = document.getElementById("desc");

    if (data.familyId) {
        const a_fam = document.createElement("a");
        a_fam.href = `/pages/language.html?id=${encodeURIComponent(data.familyId)}`;
        a_fam.textContent = data.familyName;
        family.appendChild(a_fam);
    }

    if (data.parentId) {
        const a_par = document.createElement("a");
        a_par.href = `/pages/language.html?id=${encodeURIComponent(data.parentId)}`;
        a_par.textContent = data.parentName;
        parent.appendChild(a_par);
    }

    level.textContent = data.level ?? "-";

    desc.textContent = data.description ?? "";

    name.textContent = data.name ?? "-";

    const data_s = await apiFetch(`/api/languages/${id}/scenarios`);
    const scenarios = data_s ?? [];
    const info = document.getElementById("nb_scenarios");
    info.textContent = `${scenarios.length} scenario(s) using this language`;

    for (const s of scenarios) {
        const tr = document.createElement("tr");
        const td_name = document.createElement("td");
        const td_author = document.createElement("td");
        const td_date = document.createElement("td");

        const a = document.createElement("a");
        a.href = `/pages/scenario.html?id=${encodeURIComponent(s.id)}`;
        a.textContent = s.title ?? "Untitled scenario";

        td_name.appendChild(a);
        td_author.textContent = s.authorUsername ?? "Unknown author";
        td_date.textContent = s.createdAt ?? "Unknown date";

        tr.appendChild(td_name);
        tr.appendChild(td_author);
        tr.appendChild(td_date);

        document.getElementById("scenarios").appendChild(tr);
    }
}

main().catch(e => {
    const info = document.getElementById("info");
    if (info) info.textContent = e.message;
});