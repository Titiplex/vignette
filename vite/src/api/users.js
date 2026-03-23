import {apiFetch} from "./rest";

export function fetchMyProfile() {
    return apiFetch("/api/users/me/profile");
}

export function updateMyProfile(body) {
    return apiFetch("/api/users/me/profile", {
        method: "PUT",
        body,
    });
}