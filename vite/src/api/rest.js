let accessToken = null;

export function setAccessToken(t) {
    accessToken = t;
}

function getCookie(name) {
    return document.cookie
        .split("; ")
        .find((row) => row.startsWith(name + "="))
        ?.split("=")[1];
}

export async function apiFetch(path, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set("Accept", "application/json");

    let body = options.body;

    // JSON stringify
    const isFormData = body instanceof FormData;
    const isBlob = body instanceof Blob;
    const isString = typeof body === "string";

    if (body != null && !isFormData && !isBlob && !isString) {
        if (!headers.has("Content-Type")) headers.set("Content-Type", "application/json");
        body = JSON.stringify(body);
    }

    if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);

    // CSRF cookie/session
    const xsrf = getCookie("XSRF-TOKEN");
    const method = (options.method || "GET").toUpperCase();
    const stateChanging = !["GET", "HEAD", "OPTIONS"].includes(method);
    if (stateChanging && xsrf && !headers.has("X-XSRF-TOKEN")) {
        headers.set("X-XSRF-TOKEN", decodeURIComponent(xsrf));
    }

    const res = await fetch(path, {
        ...options,
        headers,
        body,
        credentials: "include",
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