import "../style.css";
import { apiFetch, setAccessToken } from "../api/rest.js";

const t = sessionStorage.getItem("accessToken");
if (t) setAccessToken(t);

function qp(name, def = null) {
    const u = new URL(window.location.href);
    return u.searchParams.get(name) ?? def;
}

async function main() {
    const id = qp("id");
    if (!id) throw new Error("Missing scenario id");

    const s = await apiFetch(`/api/scenarios/${encodeURIComponent(id)}`);

    document.getElementById("title").textContent = s.title ?? "Scenario";
    document.getElementById("lang").textContent = s.languageId ?? "-";
    document.getElementById("author").textContent = s.author.username ?? "-";
    document.getElementById("desc").textContent = s.description ?? "";
}

main().catch(e => {
    document.getElementById("error").textContent = e.message;
});