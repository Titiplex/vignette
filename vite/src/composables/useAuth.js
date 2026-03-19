import {computed, ref} from "vue";
import {login as apiLogin, logout as apiLogout, me as apiMe} from "../api/auth";

const currentUser = ref(null);
const authLoaded = ref(false);
const authLoading = ref(false);

export function useAuth() {
    const isAuthenticated = computed(() => !!currentUser.value);

    async function loadMe(force = false) {
        if (authLoading.value) return currentUser.value;
        if (authLoaded.value && !force) return currentUser.value;

        authLoading.value = true;
        try {
            currentUser.value = await apiMe();
            return currentUser.value;
        } catch {
            currentUser.value = null;
            return null;
        } finally {
            authLoaded.value = true;
            authLoading.value = false;
        }
    }

    async function login(username, password) {
        await apiLogin(username, password);
        await loadMe(true);
    }

    async function logout() {
        try {
            await apiLogout();
        } finally {
            currentUser.value = null;
            authLoaded.value = true;
        }
    }

    return {
        currentUser,
        authLoaded,
        authLoading,
        isAuthenticated,
        loadMe,
        login,
        logout,
    };
}