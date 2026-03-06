import "../style.css";
import {apiFetch} from "../api/rest.js";
import {updateHeaderAuth} from "../api/header.js";

updateHeaderAuth().then(() => {});

function qp(name) {
    const u = new URL(window.location.href);
    return u.searchParams.get(name) ?? "";
}

const targetType = qp("targetType") || "LANGUAGE";
const targetId = qp("targetId") || "";

document.getElementById("discussionTarget").textContent = `Target: ${targetType} #${targetId}`;

function renderMessages(list) {
    const root = document.getElementById("discussionList");
    root.innerHTML = "";

    if (!list.length) {
        root.textContent = "No message yet.";
        return;
    }

    for (const m of list) {
        const card = document.createElement("div");
        card.className = "card";
        card.style.marginBottom = "10px";

        const meta = document.createElement("p");
        meta.textContent = `[${m.contributionType}] ${m.authorUsername} · ${m.createdAt}`;
        const content = document.createElement("p");
        content.textContent = m.content;

        card.append(meta, content);
        root.appendChild(card);
    }
}

async function loadMessages() {
    const list = await apiFetch(`/api/community/discussions?targetType=${encodeURIComponent(targetType)}&targetId=${encodeURIComponent(targetId)}`);
    renderMessages(list ?? []);
}

document.getElementById("postDiscussion").addEventListener("click", async () => {
    const err = document.getElementById("discussionErr");
    err.textContent = "";
    try {
        await apiFetch("/api/community/discussions", {
            method: "POST",
            body: {
                targetType,
                targetId,
                contributionType: document.getElementById("contributionType").value,
                content: document.getElementById("discussionContent").value
            }
        });
        document.getElementById("discussionContent").value = "";
        await loadMessages();
    } catch (e) {
        err.textContent = e.message;
    }
});

document.getElementById("submitAccreditationRequest").addEventListener("click", async () => {
    const err = document.getElementById("requestErr");
    err.textContent = "";
    try {
        const scopeType = document.getElementById("requestScope").value;
        const scenarioRaw = document.getElementById("requestScenarioId").value.trim();
        await apiFetch("/api/community/accreditation-requests", {
            method: "POST",
            body: {
                scopeType,
                scenarioId: scopeType === "SCENARIO" ? Number(scenarioRaw) : null,
                motivation: document.getElementById("requestMotivation").value
            }
        });
        document.getElementById("requestMotivation").value = "";
        err.textContent = "Request submitted.";
    } catch (e) {
        err.textContent = e.message;
    }
});

loadMessages().catch((e) => {
    document.getElementById("discussionErr").textContent = e.message;
});
