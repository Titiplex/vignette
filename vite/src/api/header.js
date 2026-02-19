import { apiFetch } from "./rest.js";

export async function updateHeaderAuth() {
    const loginLink = document.getElementById("loginLink");
    if (!loginLink) return;

    try {
        const me = await apiFetch("/api/auth/me");
        // User is logged in
        loginLink.textContent = me.username;
        loginLink.href = "#";
        loginLink.onclick = (e) => {
            e.preventDefault();
            sessionStorage.removeItem("accessToken");
            window.location.href = "/pages/login";
        };
    } catch (_) {
        // User is not logged in, keep default
        loginLink.textContent = "Login";
        loginLink.href = "/pages/login";
    }
}