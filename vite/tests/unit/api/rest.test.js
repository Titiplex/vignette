import {apiFetch, buildApiUrl, getAccessToken, setAccessToken,} from "@/api/rest";

function jsonResponse(body, status = 200, headers = {}) {
    return new Response(JSON.stringify(body), {
        status,
        headers: {
            "Content-Type": "application/json",
            ...headers,
        },
    });
}

function textResponse(body, status = 200, headers = {}) {
    return new Response(body, {
        status,
        headers,
    });
}

describe("rest.js", () => {
    beforeEach(() => {
        sessionStorage.clear();
        document.cookie = "XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/";
        setAccessToken(null);
        global.fetch = vi.fn();
    });

    it("buildApiUrl returns relative path when no VITE_API_BASE is set", () => {
        expect(buildApiUrl("/api/test")).toBe("/api/test");
        expect(buildApiUrl("api/test")).toBe("/api/test");
    });

    it("buildApiUrl leaves absolute URLs unchanged", () => {
        expect(buildApiUrl("https://example.com/a")).toBe("https://example.com/a");
    });

    it("apiFetch serializes JSON body and adds bearer token on non-auth routes", async () => {
        setAccessToken("abc123");
        fetch.mockResolvedValueOnce(jsonResponse({ok: true}));

        await apiFetch("/api/languages", {
            method: "POST",
            body: {name: "Chuj"},
        });

        const [url, options] = fetch.mock.calls[0];
        expect(url).toBe("/api/languages");
        expect(options.method).toBe("POST");
        expect(options.credentials).toBe("include");
        expect(options.body).toBe(JSON.stringify({name: "Chuj"}));

        const headers = new Headers(options.headers);
        expect(headers.get("Accept")).toBe("application/json");
        expect(headers.get("Content-Type")).toBe("application/json");
        expect(headers.get("Authorization")).toBe("Bearer abc123");
    });

    it("does not attach bearer token to auth endpoints", async () => {
        setAccessToken("secret");
        fetch.mockResolvedValueOnce(jsonResponse({id: 1}));

        await apiFetch("/api/auth/me");

        const [, options] = fetch.mock.calls[0];
        const headers = new Headers(options.headers);
        expect(headers.get("Authorization")).toBeNull();
    });

    it("adds XSRF header on state-changing requests when cookie exists", async () => {
        document.cookie = "XSRF-TOKEN=test-xsrf-token";
        fetch.mockResolvedValueOnce(jsonResponse({ok: true}));

        await apiFetch("/api/scenarios", {
            method: "POST",
            body: {title: "Story"},
        });

        const [, options] = fetch.mock.calls[0];
        const headers = new Headers(options.headers);
        expect(headers.get("X-XSRF-TOKEN")).toBe("test-xsrf-token");
    });

    it("refreshes token once on 401 and retries original request", async () => {
        setAccessToken("old-token");

        fetch
            .mockResolvedValueOnce(textResponse("Unauthorized", 401))
            .mockResolvedValueOnce(jsonResponse({accessToken: "new-token"}))
            .mockResolvedValueOnce(jsonResponse({items: [1, 2, 3]}));

        const result = await apiFetch("/api/languages");

        expect(result).toEqual({items: [1, 2, 3]});
        expect(fetch).toHaveBeenCalledTimes(3);

        const firstHeaders = new Headers(fetch.mock.calls[0][1].headers);
        const retryHeaders = new Headers(fetch.mock.calls[2][1].headers);

        expect(firstHeaders.get("Authorization")).toBe("Bearer old-token");
        expect(retryHeaders.get("Authorization")).toBe("Bearer new-token");
        expect(getAccessToken()).toBe("new-token");
    });

    it("clears token when refresh fails", async () => {
        setAccessToken("old-token");

        fetch
            .mockResolvedValueOnce(textResponse("Unauthorized", 401))
            .mockResolvedValueOnce(textResponse("Still unauthorized", 401));

        await expect(apiFetch("/api/languages")).rejects.toThrow();

        expect(getAccessToken()).toBeNull();
    });

    it("returns plain text when response is not JSON", async () => {
        fetch.mockResolvedValueOnce(textResponse("plain result", 200, {
            "Content-Type": "text/plain",
        }));

        const result = await apiFetch("/api/ping");
        expect(result).toBe("plain result");
    });

    it("throws best available API error message", async () => {
        fetch.mockResolvedValueOnce(jsonResponse({detail: "Bad request payload"}, 400));

        await expect(
            apiFetch("/api/scenarios", {method: "POST", body: {}})
        ).rejects.toThrow("Bad request payload");
    });
});