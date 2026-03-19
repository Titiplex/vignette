import {apiFetch} from "./rest";

export function fetchScenarios() {
    return apiFetch("/api/scenarios");
}

export function fetchScenario(id) {
    return apiFetch(`/api/scenarios/${id}`);
}

export function createScenario(body) {
    return apiFetch("/api/scenarios", {
        method: "POST",
        body,
    });
}

export function fetchScenarioThumbnails(id) {
    return apiFetch(`/api/scenarios/${id}/thumbnails`);
}

export function uploadScenarioThumbnail(id, formData) {
    return apiFetch(`/api/scenarios/${id}/thumbnails`, {
        method: "POST",
        body: formData,
    });
}

export function fetchThumbnailAudios(id) {
    return apiFetch(`/api/thumbnails/${id}/audios`);
}

export function uploadThumbnailAudio(id, formData) {
    return apiFetch(`/api/thumbnails/${id}/audios`, {
        method: "POST",
        body: formData,
    });
}