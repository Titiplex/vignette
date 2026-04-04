import {mount} from "@vue/test-utils";
import {nextTick} from "vue";
import {mockElementRect} from "../../helpers/dom";
import AudioPanel from "@/components/AudioPanel.vue";

const scenarioApiMocks = vi.hoisted(() => ({
    uploadThumbnailAudio: vi.fn(),
    updateAudioMarker: vi.fn(),
}));

const toastMocks = vi.hoisted(() => ({
    success: vi.fn(),
    error: vi.fn(),
}));

vi.mock("@/api/scenarios", () => ({
    uploadThumbnailAudio: scenarioApiMocks.uploadThumbnailAudio,
    updateAudioMarker: scenarioApiMocks.updateAudioMarker,
}));

vi.mock("@/composables/useToast", () => ({
    useToast: () => ({
        success: toastMocks.success,
        error: toastMocks.error,
    }),
}));

vi.mock("@/components/ui/BaseBadge.vue", () => ({
    default: {
        name: "BaseBadge",
        template: "<span><slot /></span>",
    },
}));

vi.mock("@/components/community/DiscussionThread.vue", () => ({
    default: {
        name: "DiscussionThread",
        template: "<div class='discussion-thread-stub'>DiscussionThread</div>",
    },
}));

describe("AudioPanel", () => {
    const selectedThumb = {
        id: 10,
        idx: 2,
        title: "River",
    };

    const audios = [
        {
            id: 5,
            idx: 1,
            title: "Birds",
            markerX: 12.3,
            markerY: 45.6,
            markerLabel: "Bird area",
        },
    ];

    beforeEach(() => {
        scenarioApiMocks.uploadThumbnailAudio.mockReset();
        scenarioApiMocks.updateAudioMarker.mockReset();
        toastMocks.success.mockReset();
        toastMocks.error.mockReset();
    });

    function mountPanel(extraProps = {}) {
        return mount(AudioPanel, {
            props: {
                selectedThumb,
                audios,
                isOwner: false,
                activeAudioId: null,
                activeAudioTitle: "",
                playerState: "idle",
                ...extraProps,
            },
        });
    }

    it("is collapsed by default and opens on header click", async () => {
        const wrapper = mountPanel();

        expect(wrapper.find(".collapsible-card__body").exists()).toBe(false);

        await wrapper.find(".collapsible-card__header").trigger("click");

        expect(wrapper.find(".collapsible-card__body").exists()).toBe(true);
        expect(wrapper.text()).toContain("Existing audio clips");
    });

    it("emits play-audio when clicking play on a clip", async () => {
        const wrapper = mountPanel();

        await wrapper.find(".collapsible-card__header").trigger("click");
        const playButton = wrapper.findAll("button").find((b) => b.text() === "Play");
        await playButton.trigger("click");

        expect(wrapper.emitted("play-audio")).toEqual([[audios[0]]]);
    });

    it("shows read-only owner message for non-owners", async () => {
        const wrapper = mountPanel({isOwner: false});

        await wrapper.find(".collapsible-card__header").trigger("click");

        expect(wrapper.text()).toContain("only the owner can upload audio and place markers");
        expect(wrapper.text()).not.toContain("Start recording");
    });

    it("lets owners place a marker draft by clicking the image", async () => {
        const wrapper = mountPanel({isOwner: true});

        await wrapper.find(".collapsible-card__header").trigger("click");

        const image = wrapper.find(".marker-image");
        mockElementRect(image.element, {
            left: 0,
            top: 0,
            width: 200,
            height: 100,
        });

        await image.trigger("click", {
            clientX: 50,
            clientY: 50,
        });

        await nextTick();

        expect(wrapper.text()).toContain("25.00%");
        expect(wrapper.text()).toContain("50.00%");
        expect(wrapper.find(".marker-dot--draft").exists()).toBe(true);
    });

    it("loads existing marker values into edit mode and saves them", async () => {
        scenarioApiMocks.updateAudioMarker.mockResolvedValueOnce({ok: true});

        const wrapper = mountPanel({isOwner: true});

        await wrapper.find(".collapsible-card__header").trigger("click");

        const editButton = wrapper.findAll("button").find((b) => b.text() === "Edit marker");
        await editButton.trigger("click");

        const saveButton = wrapper.findAll("button").find((b) => b.text() === "Save marker changes");
        await saveButton.trigger("click");

        expect(scenarioApiMocks.updateAudioMarker).toHaveBeenCalledWith(5, {
            markerX: 12.3,
            markerY: 45.6,
            markerLabel: "Bird area",
        });

        expect(toastMocks.success).toHaveBeenCalled();
        expect(wrapper.emitted("uploaded")).toBeTruthy();
    });
});