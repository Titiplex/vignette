import {logout} from "./auth.js";
import {apiFetch} from "./rest.js";

function show(el, visible) {
    if (!el) return;
    el.style.display = visible ? "" : "none";
}

export async function updateHeaderAuth() {

    const loginLink = document.getElementById("loginLink");
    const createScenarioLink = document.getElementById("createScenarioLink");
    const profileLink = document.getElementById("profileLink");

    function to_login() {
        show(createScenarioLink, false);
        show(profileLink, false);

        if (loginLink) {
            loginLink.textContent = "Login";
            loginLink.href = "/pages/login.html";
            loginLink.onclick = null;
        }
    }

    if (!loginLink && !createScenarioLink && !profileLink) return;

    try {
        const me = await apiFetch("/api/auth/me");

        if (!me) {
            to_login();
            return;
        }

        show(createScenarioLink, true);
        show(profileLink, true);

        if (loginLink) {
            loginLink.textContent = `Logout`;
            loginLink.href = "#";
            loginLink.onclick = async (e) => {
                e.preventDefault();
                try {
                    await logout();
                } catch (_) {
                    // ensure client-side state is reset even if API logout fails
                }
                sessionStorage.removeItem("accessToken");
                window.location.href = "/pages/login.html";
            };
        }
    } catch (_) {
        to_login();
    }
}
