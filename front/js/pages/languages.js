import {apiFetch} from "api/rest.js";

function qp(name, def) {
    const u = new URL(window.location.href);
    return u.searchParams.get(name) ?? def;
}

function setPage(p) {
    const u = new URL(window.location.href);
    u.searchParams.set("page", String(p));
    window.location.href = u.toString();
}

function pageWindow(current, total, windowSize = 2) {
    const start = Math.max(0, current - windowSize);
    const end = Math.min(total - 1, current + windowSize);
    return {start, end};
}

async function main() {
    const page = Number(qp("page", "0"));
    const size = 50;

    const data = await apiFetch(`/api/languages?page=${page}&size=${size}`);
    // suppose: { content: [...], number, totalPages, hasNext, hasPrevious }
    const tbody = document.getElementById("tbody");
    tbody.innerHTML = "";

    for (const l of data.content) {
        const tr = document.createElement("tr");
        tr.innerHTML = `
      <td>${l.id}</td>
      <td><a href="/front/pages/language.html?id=${encodeURIComponent(l.id)}">${l.name ?? ""}</a></td>
      <td>${l.level ?? ""}</td>
      <td>${l.familyName ?? "-"}</td>
      <td>${l.parentName ?? "-"}</td>
    `;
        tbody.appendChild(tr);
    }

    document.getElementById("pageInfo").textContent =
        `Page ${data.number + 1} / ${data.totalPages}`;

    const pager = document.getElementById("pager");
    pager.innerHTML = "";

    if (data.number > 0) {
        const a = document.createElement("a");
        a.textContent = "« First";
        a.href = "javascript:void(0)";
        a.onclick = () => setPage(0);
        pager.appendChild(a);
    }

    if (data.hasPrevious) {
        const a = document.createElement("a");
        a.style.marginLeft = "8px";
        a.textContent = "‹ Prev";
        a.href = "javascript:void(0)";
        a.onclick = () => setPage(data.number - 1);
        pager.appendChild(a);
    }

    if (data.totalPages > 0) {
        const {start, end} = pageWindow(data.number, data.totalPages, 2);
        for (let p = start; p <= end; p++) {
            const span = document.createElement("span");
            span.style.margin = "0 4px";
            if (p === data.number) {
                span.innerHTML = `<strong>${p + 1}</strong>`;
            } else {
                span.innerHTML = `<a href="javascript:void(0)">${p + 1}</a>`;
                span.querySelector("a").onclick = () => setPage(p);
            }
            pager.appendChild(span);
        }
    }

    if (data.hasNext) {
        const a = document.createElement("a");
        a.style.marginLeft = "8px";
        a.textContent = "Next ›";
        a.href = "javascript:void(0)";
        a.onclick = () => setPage(data.number + 1);
        pager.appendChild(a);
    }

    if (data.number < data.totalPages - 1) {
        const a = document.createElement("a");
        a.style.marginLeft = "8px";
        a.textContent = "Last »";
        a.href = "javascript:void(0)";
        a.onclick = () => setPage(data.totalPages - 1);
        pager.appendChild(a);
    }
}

main().catch(e => {
    document.getElementById("pageInfo").textContent = e.message;
});
