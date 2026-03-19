import { createRouter, createWebHistory } from "vue-router";

import HomeView from "../views/HomeView.vue";
import LanguagesView from "../views/LanguagesView.vue";
import LanguageDetailView from "../views/LanguageDetailView.vue";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import ScenariosView from "../views/ScenariosView.vue";
import ScenarioDetailView from "../views/ScenarioDetailView.vue";
import CreateScenarioView from "../views/CreateScenarioView.vue";
import UserView from "../views/UserView.vue";

const routes = [
    { path: "/", name: "home", component: HomeView },
    { path: "/languages", name: "languages", component: LanguagesView },
    {
        path: "/languages/:id",
        name: "language-detail",
        component: LanguageDetailView,
        props: true,
    },
    { path: "/login", name: "login", component: LoginView },
    { path: "/register", name: "register", component: RegisterView },
    { path: "/scenarios", name: "scenarios", component: ScenariosView },
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
    },
    { path: "/user", name: "user", component: UserView },
];

export default createRouter({
    history: createWebHistory(),
    routes,
});