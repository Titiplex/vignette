import { apiFetch, setAccessToken } from "api/rest.js";

const form = document.getElementById("loginForm");
const error = document.getElementById("error");

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    error.textContent = "";

    const username = form.username.value.trim();
    const password = form.password.value;

    try {
        const res = await apiFetch("/api/auth/login", {
            method: "POST",
            body: { username, password },
        });
        setAccessToken(res.accessToken);

        sessionStorage.setItem("accessToken", res.accessToken);

        window.location.href = "/front/pages/home.html";
    } catch (err) {
        error.textContent = err.message;
    }
});

const t = sessionStorage.getItem("accessToken");
if (t) setAccessToken(t);
