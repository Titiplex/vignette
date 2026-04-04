import {apiFetch} from "./rest";

function normalizeListParams(input, page = 0, size = 50) {
    if (input instanceof URLSearchParams) {
        return input;
    }

    const params = new URLSearchParams();
    params.set("q", input ?? "");
    params.set("page", String(page));
    params.set("size", String(size));
    return params;
}

export function fetchLanguages(input = "", page = 0, size = 50) {
    const params = normalizeListParams(input, page, size);
    return apiFetch(`/api/languages?${params.toString()}`);
}

export function fetchLanguage(id) {
    return apiFetch(`/api/languages/${id}`);
}

export function fetchLanguageScenarios(id) {
    return apiFetch(`/api/languages/${id}/scenarios`);
}

export function fetchLanguageOptions(params) {
    return apiFetch(`/api/languages/options?${params.toString()}`);
}

export function fetchMyLanguagePermissions(id) {
    return apiFetch(`/api/languages/${id}/permissions/me`);
}

export function updateLanguage(id, body) {
    return apiFetch(`/api/languages/${id}`, {
        method: "PUT",
        body,
    });
}