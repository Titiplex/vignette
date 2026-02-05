let accessToken = null;

export function setAccessToken(token) {
    accessToken = token;
}

function getCsrfTokenFromCookie() {
    // CookieCsrfTokenRepository => cookie "XSRF-TOKEN"
    const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    return m ? decodeURIComponent(m[1]) : null;
}

export async function apiFetch(path, { method = "GET", body, headers = {} } = {}) {
    const opts = {
        method,
        credentials: "include", // IMPORTANT pour session cookie
        headers: {
            "Accept": "application/json",
            ...headers,
        },
    };

    if (body !== undefined) {
        opts.headers["Content-Type"] = "application/json";
        opts.body = JSON.stringify(body);

        // CSRF
        const csrf = getCsrfTokenFromCookie();
        if (csrf && !(opts.headers["Authorization"] || "").startsWith("Bearer ")) {
            opts.headers["X-XSRF-TOKEN"] = csrf;
        }
    }

    if (accessToken && !opts.headers["Authorization"]) {
        opts.headers["Authorization"] = `Bearer ${accessToken}`;
    }

    const res = await fetch(path, opts);

    if (res.status === 204) return null;

    const isJson = (res.headers.get("content-type") || "").includes("application/json");
    const data = isJson ? await res.json() : await res.text();

    if (!res.ok) {
        const msg = isJson && data && data.message ? data.message : `HTTP ${res.status}`;
        throw new Error(msg);
    }
    return data;
}