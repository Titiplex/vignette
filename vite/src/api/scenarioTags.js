import {apiFetch} from "./rest";

export function fetchScenarioTagSuggestions(query = "", limit = 10) {
    const params = new URLSearchParams({
        q: query,
        limit: String(limit),
    });

    return apiFetch(`/api/scenario/tags?${params.toString()}`);
}