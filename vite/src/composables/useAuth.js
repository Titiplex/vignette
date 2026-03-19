import {computed, ref} from "vue";
import {login as apiLogin, logout as apiLogout, me as apiMe} from "../api/auth";

const currentUser = ref(null);
const authLoaded = ref(false);

export function useAuth() {
    const isAuthenticated = computed(() => !!currentUser.value);

    async function loadMe() {
        try {
            currentUser.value = await apiMe();
        } catch {
            currentUser.value = null;
        } finally {
            authLoaded.value = true;
        }
    }

    async function login(username, password) {
        await apiLogin(username, password);
        await loadMe();
    }

    async function logout() {
        try {
            await apiLogout();
        } finally {
            currentUser.value = null;
        }
    }

    return {
        currentUser,
        authLoaded,
        isAuthenticated,
        loadMe,
        login,
        logout,
    };
}