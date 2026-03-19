import {apiFetch} from "./rest";

export function fetchMyProfile() {
    return apiFetch("/api/users/me");
}

export function updateMyProfile(body) {
    return apiFetch("/api/users/me", {
        method: "PUT",
        body,
    });
}