import {mount} from "@vue/test-utils";
import {createMemoryHistory, createRouter} from "vue-router";

export async function mountWithRouter(component, {
    routes = [{path: "/", component: {template: "<div />"}}],
    initialRoute = "/",
    mountOptions = {},
} = {}) {
    const router = createRouter({
        history: createMemoryHistory(),
        routes,
    });

    await router.push(initialRoute);
    await router.isReady();

    const wrapper = mount(component, {
        global: {
            plugins: [router],
            ...(mountOptions.global || {}),
        },
        ...mountOptions,
    });

    return {wrapper, router};
}