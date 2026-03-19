let accessToken = sessionStorage.getItem("accessToken") || null;

export function setAccessToken(token) {
    accessToken = token;
    if (token) {
        sessionStorage.setItem("accessToken", token);
    } else {
        sessionStorage.removeItem("accessToken");
    }
}

export function getAccessToken() {
    return accessToken;
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
    const method = (options.method || "GET").toUpperCase();

    const isFormData = body instanceof FormData;
    const isBlob = body instanceof Blob;
    const isString = typeof body === "string";

    if (body != null && !isFormData && !isBlob && !isString) {
        if (!headers.has("Content-Type")) {
            headers.set("Content-Type", "application/json");
        }
        body = JSON.stringify(body);
    }

    if (accessToken) {
        headers.set("Authorization", `Bearer ${accessToken}`);
    }

    const xsrf = getCookie("XSRF-TOKEN");
    const stateChanging = !["GET", "HEAD", "OPTIONS"].includes(method);
    if (stateChanging && xsrf && !headers.has("X-XSRF-TOKEN")) {
        headers.set("X-XSRF-TOKEN", decodeURIComponent(xsrf));
    }

    const res = await fetch(path, {
        ...options,
        method,
        headers,
        body,
        credentials: "include",
    });

    if (!res.ok) {
        let message = `HTTP ${res.status}`;
        try {
            const ct = res.headers.get("content-type") || "";
            if (ct.includes("application/json")) {
                const data = await res.json();
                message = data.message || data.error || JSON.stringify(data);
            } else {
                const text = await res.text();
                if (text) message = text;
            }
        } catch {
            // ignore parse issues
        }
        throw new Error(message);
    }

    if (res.status === 204) return null;

    const ct = res.headers.get("content-type") || "";
    if (ct.includes("application/json")) return res.json();
    return res.text();
}