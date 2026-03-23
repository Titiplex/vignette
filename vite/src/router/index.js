import {createRouter, createWebHistory} from "vue-router";

import HomeView from "../views/HomeView.vue";
import LanguagesView from "../views/LanguagesView.vue";
import LanguageDetailView from "../views/LanguageDetailView.vue";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import ScenariosView from "../views/ScenariosView.vue";
import ScenarioDetailView from "../views/ScenarioDetailView.vue";
import CreateScenarioView from "../views/CreateScenarioView.vue";
import UserView from "../views/UserView.vue";

import {useAuth} from "../composables/useAuth";

const routes = [
    {path: "/", name: "home", component: HomeView},
    {path: "/languages", name: "languages", component: LanguagesView},
    {
        path: "/languages/:id",
        name: "language-detail",
        component: LanguageDetailView,
        props: true,
    },
    {path: "/login", name: "login", component: LoginView, meta: {guestOnly: true}},
    {path: "/register", name: "register", component: RegisterView, meta: {guestOnly: true}},
    {path: "/scenarios", name: "scenarios", component: ScenariosView},
    {
        path: "/scenarios/:id",
        name: "scenario-detail",
        component: ScenarioDetailView,
        props: true,
    },
    {
        path: "/create-scenario",
        name: "create-scenario",
        component: CreateScenarioView,
        meta: {requiresAuth: true},
    },
    {
        path: "/user",
        name: "user",
        component: UserView,
        meta: {requiresAuth: true},
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach(async (to) => {
    const {isAuthenticated, authLoaded, loadMe} = useAuth();

    if (!authLoaded.value) {
        await loadMe();
    }

    if (to.meta.requiresAuth && !isAuthenticated.value) {
        return {path: "/login", query: {redirect: to.fullPath}};
    }

    if (to.meta.guestOnly && isAuthenticated.value) {
        return {path: "/"};
    }

    return true;
});

export default router;