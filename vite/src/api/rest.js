let accessToken = null;

export function setAccessToken(t) {
    accessToken = t;
}

export async function apiFetch(path, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set("Accept", "application/json");

    if (options.body != null && !(options.body instanceof FormData)) {
        if (!headers.has("Content-Type")) headers.set("Content-Type", "application/json");
    }

    if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);

    const res = await fetch(path, {
        ...options,
        headers,
        credentials: "include"
    });

    if (!res.ok) {
        const txt = await res.text().catch(() => "");
        throw new Error(txt || `HTTP ${res.status}`);
    }

    if (res.status === 204) return null;

    const ct = res.headers.get("content-type") || "";
    if (ct.includes("application/json")) return res.json();
    return res.text();
}