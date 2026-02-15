import "../style.css";
import {apiFetch} from "../api/rest.js";

function qp(id) {
    const u = new URL(window.location.href);
    return u.searchParams.get(id) ?? "";
}

async function main() {
    const id = String(qp("id"));
    const data = await apiFetch(`/api/languages/${id}`);
    console.log(data);

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
}

main().catch(e => {
    const info = document.getElementById("info");
    if (info) info.textContent = e.message;
});