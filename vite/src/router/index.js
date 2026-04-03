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
import AboutProjectView from "../views/AboutProjectView.vue";

const routes = [
    {
        path: "/",
        name: "home",
        component: HomeView,
        meta: {
            title: "Home",
            description: "Landing page for the Vignette project and platform overview.",
        },
    },
    {
        path: "/about",
        name: "about",
        component: AboutProjectView,
        meta: {
            title: "About the project",
            description: "Origins, goals and design rationale of the Vignette project.",
        },
    },
    {
        path: "/languages",
        name: "languages",
        component: LanguagesView,
        meta: {
            title: "Languages",
            description: "Browse and search language entries.",
        },
    },
    {
        path: "/languages/:id",
        name: "language-detail",
        component: LanguageDetailView,
        props: true,
        meta: {
            title: "Language details",
            description: "Detailed language entry with metadata, scenarios and discussion.",
        },
    },
    {
        path: "/login",
        name: "login",
        component: LoginView,
        meta: {
            guestOnly: true,
            title: "Login",
            description: "Authenticate to access protected features.",
        },
    },
    {
        path: "/register",
        name: "register",
        component: RegisterView,
        meta: {
            guestOnly: true,
            title: "Register",
            description: "Create a new Vignette user account.",
        },
    },
    {
        path: "/scenarios",
        name: "scenarios",
        component: ScenariosView,
        meta: {
            title: "Scenarios",
            description: "Browse available scenarios and storyboard resources.",
        },
    },
    {
        path: "/scenarios/:id",
        name: "scenario-detail",
        component: ScenarioDetailView,
        props: true,
        meta: {
            title: "Scenario workspace",
            description: "Detailed scenario page with storyboard, playback and media panels.",
        },
    },
    {
        path: "/create-scenario",
        name: "create-scenario",
        component: CreateScenarioView,
        meta: {
            requiresAuth: true,
            title: "Create scenario",
            description: "Create a new scenario and initialize its metadata.",
        },
    },
    {
        path: "/user",
        name: "user",
        component: UserView,
        meta: {
            requiresAuth: true,
            title: "My profile",
            description: "Manage your user profile, affiliations and personal work.",
        },
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

    if (to.meta?.title) {
        document.title = `Vignette · ${to.meta.title}`;
    } else {
        document.title = "Vignette";
    }

    return true;
});

export default router;