import {mount} from "@vue/test-utils";
import ThumbnailCard from "@/components/ThumbnailCard.vue";

vi.mock("@/components/ui/BaseBadge.vue", () => ({
    default: {
        name: "BaseBadge",
        template: "<span><slot /></span>",
    },
}));

describe("ThumbnailCard", () => {
    const thumb = {
        id: 12,
        idx: 3,
        title: "Forest scene",
    };

    it("emits select when card is clicked", async () => {
        const wrapper = mount(ThumbnailCard, {
            props: {
                thumb,
                audios: [],
            },
        });

        await wrapper.find("article").trigger("click");

        expect(wrapper.emitted("select")).toEqual([[thumb]]);
    });

    it("emits select and play when play button is clicked", async () => {
        const wrapper = mount(ThumbnailCard, {
            props: {
                thumb,
                audios: [],
            },
        });

        await wrapper.find(".storyboard-tile__play").trigger("click");

        expect(wrapper.emitted("select")).toEqual([[thumb]]);
        expect(wrapper.emitted("play")).toEqual([[thumb]]);
    });

    it("renders only valid markers and clamps coordinates", () => {
        const wrapper = mount(ThumbnailCard, {
            props: {
                thumb,
                audios: [
                    {id: 1, markerX: 25, markerY: 50, title: "ok"},
                    {id: 2, markerX: 120, markerY: -10, title: "clamped"},
                    {id: 3, markerX: null, markerY: 40, title: "invalid"},
                    {id: 4, markerX: "", markerY: "", title: "invalid-2"},
                ],
            },
        });

        const markers = wrapper.findAll(".storyboard-tile__marker");
        expect(markers).toHaveLength(2);

        expect(markers[0].attributes("style")).toContain("left: 25%");
        expect(markers[0].attributes("style")).toContain("top: 50%");

        expect(markers[1].attributes("style")).toContain("left: 100%");
        expect(markers[1].attributes("style")).toContain("top: 0%");
    });
});