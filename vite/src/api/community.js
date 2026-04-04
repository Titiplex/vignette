import {apiFetch} from "./rest";

export const CONTRIBUTION_TYPES = [
    {value: "GENERAL", label: "General"},
    {value: "TRANSCRIPTION", label: "Transcription"},
    {value: "TRANSLATION", label: "Translation"},
    {value: "GLOSS", label: "Gloss"},
    {value: "INTERPRETATION", label: "Interpretation"},
];

export function fetchDiscussionMessages(targetType, targetId) {
    const params = new URLSearchParams({
        targetType,
        targetId: String(targetId),
    });

    return apiFetch(`/api/community/discussions?${params.toString()}`);
}

export function createDiscussionMessage(body) {
    return apiFetch("/api/community/discussions", {
        method: "POST",
        body,
    });
}

export function createAccreditationRequest(body) {
    return apiFetch("/api/community/accreditation-requests", {
        method: "POST",
        body,
    });
}

export function fetchAccreditationRequests(permissionType, scopeType, targetId) {
    const params = new URLSearchParams({
        permissionType,
        scopeType,
    });

    if (targetId != null && targetId !== "") {
        params.set("targetId", String(targetId));
    }

    return apiFetch(`/api/community/accreditation-requests?${params.toString()}`);
}

export function reviewAccreditationRequest(requestId, body) {
    return apiFetch(`/api/community/accreditation-requests/${requestId}/review`, {
        method: "POST",
        body,
    });
}

export function fetchAccreditations(permissionType, scopeType, targetId) {
    const params = new URLSearchParams({
        scopeType,
    });

    if (permissionType) {
        params.set("permissionType", permissionType);
    }

    if (targetId != null && targetId !== "") {
        params.set("targetId", String(targetId));
    }

    return apiFetch(`/api/community/accreditations?${params.toString()}`);
}

export function grantAccreditation(body) {
    return apiFetch("/api/community/accreditations", {
        method: "POST",
        body,
    });
}