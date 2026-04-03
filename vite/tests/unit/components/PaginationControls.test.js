import {mount} from "@vue/test-utils";
import PaginationControls from "@/components/PaginationControls.vue";

describe("PaginationControls", () => {
    it("does not render when there is only one page", () => {
        const wrapper = mount(PaginationControls, {
            props: {
                page: 0,
                totalPages: 1,
            },
        });

        expect(wrapper.find(".pager").exists()).toBe(false);
    });

    it("renders pages around the current one", () => {
        const wrapper = mount(PaginationControls, {
            props: {
                page: 5,
                totalPages: 10,
            },
        });

        const pageButtons = wrapper.findAll(".pager__page").map((b) => b.text());
        expect(pageButtons).toEqual(["4", "5", "6", "7", "8"]);
    });

    it("disables first and prev on first page", () => {
        const wrapper = mount(PaginationControls, {
            props: {
                page: 0,
                totalPages: 5,
            },
        });

        const buttons = wrapper.findAll("button");
        expect(buttons[0].attributes("disabled")).toBeDefined();
        expect(buttons[1].attributes("disabled")).toBeDefined();
    });

    it("emits go when clicking navigation buttons", async () => {
        const wrapper = mount(PaginationControls, {
            props: {
                page: 2,
                totalPages: 5,
            },
        });

        const buttons = wrapper.findAll("button");
        await buttons[3].trigger("click"); // page 2 in visible pages => emits specific page
        await buttons[buttons.length - 2].trigger("click"); // Next

        const emitted = wrapper.emitted("go");
        expect(emitted).toBeTruthy();
        expect(emitted[0]).toEqual([1]);
        expect(emitted[1]).toEqual([3]);
    });
});