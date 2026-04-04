import {apiFetch} from "./rest";

export function fetchAdminOverview() {
    return apiFetch("/api/admin/overview");
}

export function fetchAdminUsers() {
    return apiFetch("/api/admin/users");
}

export function updateAdminUserRoles(id, roles) {
    return apiFetch(`/api/admin/users/${id}/roles`, {
        method: "PATCH",
        body: {roles},
    });
}

export function fetchAdminScenarios() {
    return apiFetch("/api/admin/scenarios");
}

export function updateAdminScenarioVisibility(id, visibilityStatus) {
    return apiFetch(`/api/admin/scenarios/${id}/visibility`, {
        method: "PATCH",
        body: {visibilityStatus},
    });
}