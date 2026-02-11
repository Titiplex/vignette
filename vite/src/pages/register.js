import { apiFetch } from "../api/rest.js";

const form = document.getElementById("registerForm");
const error = document.getElementById("error");

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    error.textContent = "";

    const body = {
        username: form.username.value.trim(),
        email: form.email.value.trim(),
        password: form.password.value,
    };

    try {
        await apiFetch("/api/auth/register", { method: "POST", body });
        window.location.href = "/front/pages/login.html";
    } catch (err) {
        error.textContent = err.message;
    }
});
