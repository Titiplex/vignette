import "../style.css";
import {apiFetch} from "../api/rest.js";
import {updateHeaderAuth} from "../api/header.js";

updateHeaderAuth().then(() => {});

function scopeParams() {
    const scopeType = document.getElementById("adminScope").value;
    const scenarioRaw = document.getElementById("adminScenarioId").value.trim();
    return {
        scopeType,
        scenarioId: scopeType === "SCENARIO" ? Number(scenarioRaw) : null
    };
}

function setText(id, txt) {
    const el = document.getElementById(id);
    if (el) el.textContent = txt;
}

function renderRequests(list) {
    const root = document.getElementById("requestsList");
    root.innerHTML = "";
    for (const req of list) {
        const box = document.createElement("div");
        box.className = "card";
        box.style.marginBottom = "10px";
        box.innerHTML = `<p>#${req.id} · ${req.requestedByUsername} · ${req.status}</p><p>${req.motivation}</p>`;

        if (req.status === "PENDING") {
            const approve = document.createElement("button");
            approve.textContent = "Approve";
            approve.onclick = () => review(req.id, true);

            const reject = document.createElement("button");
            reject.textContent = "Reject";
            reject.className = "secondary-btn";
            reject.style.marginLeft = "8px";
            reject.onclick = () => review(req.id, false);

            box.append(approve, reject);
        }

        root.appendChild(box);
    }
}

function renderAccreditations(list) {
    const root = document.getElementById("accreditationsList");
    root.innerHTML = "";
    for (const a of list) {
        const p = document.createElement("p");
        p.textContent = `#${a.id} · user ${a.username} (${a.userId}) · granted at ${a.grantedAt}`;
        root.appendChild(p);
    }
}

async function review(id, approved) {
    try {
        await apiFetch(`/api/community/accreditation-requests/${id}/review`, {
            method: "POST",
            body: {approved, reviewNote: approved ? "Approved by reviewer" : "Rejected by reviewer"}
        });
        await load();
    } catch (e) {
        setText("adminErr", e.message);
    }
}

async function load() {
    setText("adminErr", "");
    const {scopeType, scenarioId} = scopeParams();
    const q = new URLSearchParams({scopeType});
    if (scopeType === "SCENARIO") q.set("scenarioId", String(scenarioId));

    const reqs = await apiFetch(`/api/community/accreditation-requests?${q.toString()}`);
    const grants = await apiFetch(`/api/community/accreditations?${q.toString()}`);
    renderRequests(reqs ?? []);
    renderAccreditations(grants ?? []);
}

document.getElementById("reloadAdminData").addEventListener("click", () => {
    load().catch((e) => setText("adminErr", e.message));
});

document.getElementById("grantAccreditationBtn").addEventListener("click", async () => {
    const {scopeType, scenarioId} = scopeParams();
    const userId = Number(document.getElementById("grantUserId").value);
    const note = document.getElementById("grantNote").value;

    try {
        await apiFetch("/api/community/accreditations", {
            method: "POST",
            body: {userId, scopeType, scenarioId, note}
        });
        await load();
    } catch (e) {
        setText("adminErr", e.message);
    }
});

load().catch((e) => setText("adminErr", e.message));
