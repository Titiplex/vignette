import {apiFetch, setAccessToken} from "./rest.js";

export async function login(username, password) {
    const data = await apiFetch("/api/auth/login", {
        method: "POST",
        body: {username, password},
    });

    setAccessToken(data.accessToken);
    return data;
}

export async function refresh() {
    const data = await apiFetch("/api/auth/refresh", {method: "POST"});
    setAccessToken(data.accessToken);
    return data;
}

export async function logout() {
    await apiFetch("/api/auth/logout", {method: "POST"});
    setAccessToken(null);
}

export async function me() {
    return apiFetch("/api/auth/me");
}

export async function register(body) {
    return apiFetch("/api/auth/register", {
        method: "POST",
        body,
    });
}