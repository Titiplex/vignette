import { apiFetch } from "./rest.js";

function show(el, visible) {
    if (!el) return;
    el.style.display = visible ? "" : "none";
}

export async function updateHeaderAuth() {
    const loginLink = document.getElementById("loginLink");
    const createScenarioLink = document.getElementById("createScenarioLink");

    if (!loginLink && !createScenarioLink) return;

    try {
        const me = await apiFetch("/api/auth/me");

        show(createScenarioLink, true);

        if (loginLink) {
            loginLink.textContent = `Logout (${me.username})`;
            loginLink.href = "#";
            loginLink.onclick = (e) => {
                e.preventDefault();
                sessionStorage.removeItem("accessToken");
                window.location.href = "/pages/login.html";
            };
        }
    } catch (_) {
        show(createScenarioLink, false);

        if (loginLink) {
            loginLink.textContent = "Login";
            loginLink.href = "/pages/login.html";
            loginLink.onclick = null;
        }
    }
}
