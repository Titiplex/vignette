import {apiFetch, setAccessToken} from "./rest.js";

export async function login(username, password) {
    const data = await apiFetch("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({username, password})
    });
    // data = { accessToken: "..." }
    setAccessToken(data.accessToken);
}

export async function refresh() {
    const data = await apiFetch("/api/auth/refresh", {method: "POST"});
    setAccessToken(data.accessToken);
}

export async function logout() {
    await apiFetch("/api/auth/logout", {method: "POST"});
    setAccessToken(null);
}