import {defineConfig} from "vite";
import {resolve} from "path";

export default defineConfig({
    server: {
        proxy: {
            "/api": {
                target: "http://localhost:8081",
                changeOrigin: true
            }
        }
    },
    build: {
        rollupOptions: {
            input: {
                index: resolve(__dirname, "index.html"),
                languages: resolve(__dirname, "pages/languages.html"),
                language: resolve(__dirname, "pages/language.html"),
                login: resolve(__dirname, "pages/login.html"),
                register: resolve(__dirname, "pages/register.html"),
                create_scenario: resolve(__dirname, "pages/create_scenario.html"),
                scenario: resolve(__dirname, "pages/scenario.html"),
                scenarios: resolve(__dirname, "pages/scenarios.html"),
                user: resolve(__dirname, "pages/user.html"),
                community_discussions: resolve(__dirname, "pages/community_discussions.html"),
                community_admin: resolve(__dirname, "pages/community_admin.html"),
            }
        }
    }
});
