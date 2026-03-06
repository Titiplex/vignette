import "../style.css";
import {apiFetch} from "../api/rest.js";
import {updateHeaderAuth} from "../api/header.js";

updateHeaderAuth().then(() => {
});

function qp(name, def) {
    const u = new URL(window.location.href);
    return u.searchParams.get(name) ?? def;
}

function updateParams(params) {
    const u = new URL(window.location.href);
    Object.entries(params).forEach(([key, value]) => {
        if (value === null || value === undefined || value === "") {
            u.searchParams.delete(key);
        } else {
            u.searchParams.set(key, String(value));
        }
    });
    window.location.href = u.toString();
}

function setPage(p) {
    updateParams({page: p});
}

function setSearch(q) {
    updateParams({q, page: 0});
}

function pageWindow(current, total, windowSize = 2) {
    const start = Math.max(0, current - windowSize);
    const end = Math.min(total - 1, current + windowSize);
    return {start, end};
}

async function main() {
    const page = Number(qp("page", "0"));
    const q = qp("q", "").trim();
    const size = 50;

    const query = new URLSearchParams({
        page: String(page),
        size: String(size),
    });
    if (q) query.set("q", q);

    const data = await apiFetch(`/api/languages?${query.toString()}`);

    const searchInput = document.getElementById("languageSearch");
    if (searchInput) {
        searchInput.value = q;
        searchInput.addEventListener("change", () => setSearch(searchInput.value.trim()));
        searchInput.addEventListener("keydown", (e) => {
            if (e.key === "Enter") setSearch(searchInput.value.trim());
        });
    }

    const tbody = document.getElementById("tbody");
    tbody.innerHTML = "";

    for (const l of data.content) {
        const tr = document.createElement("tr");
        tr.innerHTML = `
      <td>${l.id}</td>
      <td><a href="/pages/language.html?id=${encodeURIComponent(l.id)}">${l.name ?? ""}</a></td>
      <td>${l.level ?? ""}</td>
      <td>${l.family ?? "-"}</td>
      <td>${l.parent ?? "-"}</td>
    `;
        tbody.appendChild(tr);
    }

    const totalPages = data.totalPages ?? 0;
    document.getElementById("pageInfo").textContent =
        totalPages > 0 ? `Page ${data.number + 1} / ${totalPages}` : "No pages";

    const hasPrevious = data.number > 0;
    const hasNext = data.number < totalPages - 1;

    const pager = document.getElementById("pager");
    pager.innerHTML = "";

    if (data.number > 0) {
        const a = document.createElement("a");
        a.textContent = "« First";
        a.href = "#";
        a.onclick = (e) => {
            e.preventDefault();
            setPage(0);
        };
        pager.appendChild(a);
    }

    if (hasPrevious) {
        const a = document.createElement("a");
        a.style.marginLeft = "8px";
        a.textContent = "‹ Prev";
        a.href = "#";
        a.onclick = (e) => {
            e.preventDefault();
            setPage(data.number - 1);
        };
        pager.appendChild(a);
    }

    if (totalPages > 0) {
        const {start, end} = pageWindow(data.number, totalPages, 2);
        for (let p = start; p <= end; p++) {
            const span = document.createElement("span");
            span.style.margin = "0 4px";

            if (p === data.number) {
                span.innerHTML = `<strong>${p + 1}</strong>`;
            } else {
                const a = document.createElement("a");
                a.href = "#";
                a.textContent = String(p + 1);
                a.onclick = (e) => {
                    e.preventDefault();
                    setPage(p);
                };
                span.appendChild(a);
            }
            pager.appendChild(span);
        }
    }

    if (hasNext) {
        const a = document.createElement("a");
        a.style.marginLeft = "8px";
        a.textContent = "Next ›";
        a.href = "#";
        a.onclick = (e) => {
            e.preventDefault();
            setPage(data.number + 1);
        };
        pager.appendChild(a);
    }

    if (data.number < totalPages - 1) {
        const a = document.createElement("a");
        a.style.marginLeft = "8px";
        a.textContent = "Last »";
        a.href = "#";
        a.onclick = (e) => {
            e.preventDefault();
            setPage(totalPages - 1);
        };
        pager.appendChild(a);
    }
}

main().catch(e => {
    const el = document.getElementById("pageInfo");
    if (el) el.textContent = e.message;
});
