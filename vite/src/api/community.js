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