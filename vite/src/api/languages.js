import {apiFetch} from "./rest";

export function fetchLanguages(params) {
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