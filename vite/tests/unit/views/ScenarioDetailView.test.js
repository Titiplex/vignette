import {nextTick} from "vue";
import {mountWithRouter} from "../../helpers/mountWithRouter";
import ScenarioDetailView from "@/views/ScenarioDetailView.vue";

const apiMocks = vi.hoisted(() => ({
    loadMe: vi.fn(),

    fetchScenario: vi.fn(),
    fetchScenarioThumbnails: vi.fn(),
    fetchThumbnailAudios: vi.fn(),
    fetchLanguage: vi.fn(),
    publishScenario: vi.fn(),
    updateScenarioStoryboard: vi.fn(),
    updateThumbnailLayout: vi.fn(),
    uploadScenarioThumbnail: vi.fn(),

    toastSuccess: vi.fn(),
    toastError: vi.fn(),
    toastInfo: vi.fn(),
}));

const autoplayApi = vi.hoisted(() => ({
    currentIndex: {value: -1},
    currentItem: {value: null},
    currentTime: {value: 0},
    duration: {value: 0},
    progressPercent: 0,
    isPlaying: false,
    isPaused: false,
    isLoading: false,
    autoContinue: {value: true},
    loopScenario: {value: false},
    playFromStart: vi.fn(),
    playFromIndex: vi.fn(async (index) => {
        autoplayApi.currentIndex.value = index;
    }),
    replayCurrent: vi.fn(),
    resume: vi.fn(),
    pause: vi.fn(),
    stop: vi.fn(),
    next: vi.fn(),
    previous: vi.fn(),
    seekToPercent: vi.fn(),
    seekToSeconds: vi.fn(),
    formatTime: vi.fn((value) => `t:${value}`),
    toggleAutoContinue: vi.fn(() => {
        autoplayApi.autoContinue.value = !autoplayApi.autoContinue.value;
    }),
    toggleLoopScenario: vi.fn(() => {
        autoplayApi.loopScenario.value = !autoplayApi.loopScenario.value;
    }),
}));

vi.mock("@/api/languages", () => ({
    fetchLanguage: apiMocks.fetchLanguage,
}));

vi.mock("@/api/scenarios", () => ({
    fetchScenario: apiMocks.fetchScenario,
    fetchScenarioThumbnails: apiMocks.fetchScenarioThumbnails,
    fetchThumbnailAudios: apiMocks.fetchThumbnailAudios,
    publishScenario: apiMocks.publishScenario,
    updateScenarioStoryboard: apiMocks.updateScenarioStoryboard,
    updateThumbnailLayout: apiMocks.updateThumbnailLayout,
    uploadScenarioThumbnail: apiMocks.uploadScenarioThumbnail,
}));

vi.mock("@/composables/useAuth", () => ({
    useAuth: () => ({
        currentUser: {
            value: {
                id: 50,
                username: "ownerUser",
            },
        },
        loadMe: apiMocks.loadMe,
    }),
}));

vi.mock("@/composables/useToast", () => ({
    useToast: () => ({
        success: apiMocks.toastSuccess,
        error: apiMocks.toastError,
        info: apiMocks.toastInfo,
    }),
}));

vi.mock("@/composables/useScenarioAutoplay", () => ({
    useScenarioAutoplay: () => autoplayApi,
}));

vi.mock("@/components/ThumbnailCard.vue", () => ({
    default: {
        name: "ThumbnailCard",
        props: ["thumb", "audios", "selected", "highlighted"],
        emits: ["select", "play"],
        template: `
          <button
              class="thumbnail-card-stub"
              :data-id="thumb.id"
              @click="$emit('select', thumb)"
          >
            {{ thumb.title || thumb.id }}
          </button>
        `,
    },
}));

vi.mock("@/components/AudioPanel.vue", () => ({
    default: {
        name: "AudioPanel",
        props: ["selectedThumb", "audios", "activeAudioId", "activeAudioTitle", "playerState", "isOwner"],
        emits: ["uploaded", "play-audio"],
        template: `
          <div class="audio-panel-stub">
            <span class="audio-panel-thumb">{{ selectedThumb?.id ?? 'none' }}</span>
            <span class="audio-panel-audios">{{ audios.length }}</span>
            <button class="audio-panel-uploaded" @click="$emit('uploaded')">uploaded</button>
            <button
                class="audio-panel-play"
                @click="$emit('play-audio', audios[0] || null)"
            >
              play
            </button>
          </div>
        `,
    },
}));

vi.mock("@/components/ui/BasePageHeader.vue", () => ({
    default: {
        name: "BasePageHeader",
        props: ["title", "subtitle"],
        template: `
          <section class="base-page-header-stub">
            <h1>{{ title }}</h1>
            <p>{{ subtitle }}</p>
            <div>
              <slot/>
            </div>
            <div>
              <slot name="actions"/>
            </div>
          </section>
        `,
    },
}));

vi.mock("@/components/ui/BaseLoader.vue", () => ({
    default: {
        name: "BaseLoader",
        template: `<div class="base-loader-stub"><slot /></div>`,
    },
}));

vi.mock("@/components/ui/BaseAlert.vue", () => ({
    default: {
        name: "BaseAlert",
        props: ["type"],
        template: `
          <div class="base-alert-stub" :data-type="type">
            <slot/>
          </div>`,
    },
}));

vi.mock("@/components/ui/BaseEmptyState.vue", () => ({
    default: {
        name: "BaseEmptyState",
        props: ["title", "message"],
        template: `
          <div class="empty-state-stub">{{ title }} - {{ message }}</div>`,
    },
}));

vi.mock("@/components/ui/BaseBadge.vue", () => ({
    default: {
        name: "BaseBadge",
        props: ["variant"],
        template: `<span class="base-badge-stub" :data-variant="variant"><slot/></span>`,
    },
}));

function baseScenario(overrides = {}) {
    return {
        id: 77,
        title: "Scenario Alpha",
        description: "A useful scenario",
        authorUsername: "ownerUser",
        languageId: 42,
        visibilityStatus: "DRAFT",
        storyboardLayoutMode: "PRESET",
        storyboardPreset: "GRID_3",
        storyboardColumns: 3,
        ...overrides,
    };
}

function baseThumbnails() {
    return [
        {
            id: 10,
            idx: 2,
            title: "Second",
            imageWidth: 1000,
            imageHeight: 800,
            gridColumn: null,
            gridRow: null,
            gridColumnSpan: 1,
            gridRowSpan: 1,
        },
        {
            id: 5,
            idx: 1,
            title: "First",
            imageWidth: 800,
            imageHeight: 1200,
            gridColumn: 2,
            gridRow: 3,
            gridColumnSpan: 2,
            gridRowSpan: 1,
        },
    ];
}

function audioMapByThumb() {
    return {
        5: [
            {
                id: 501,
                idx: 2,
                title: "First-thumb audio B",
                markerX: 33,
                markerY: 40,
                markerLabel: "B",
            },
            {
                id: 500,
                idx: 1,
                title: "First-thumb audio A",
                markerX: 11,
                markerY: 22,
                markerLabel: "A",
            },
        ],
        10: [
            {
                id: 1000,
                idx: 1,
                title: "Second-thumb audio",
                markerX: null,
                markerY: null,
                markerLabel: null,
            },
        ],
    };
}

async function flushPromises(times = 8) {
    for (let i = 0; i < times; i += 1) {
        await Promise.resolve();
        await nextTick();
    }
}

async function mountScenarioView({
                                     scenario = baseScenario(),
                                     thumbnails = baseThumbnails(),
                                     audioMap = audioMapByThumb(),
                                 } = {}) {
    apiMocks.fetchScenario.mockResolvedValue(scenario);
    apiMocks.fetchLanguage.mockResolvedValue({id: 42, name: "Chuj"});
    apiMocks.fetchScenarioThumbnails.mockResolvedValue(thumbnails);
    apiMocks.fetchThumbnailAudios.mockImplementation(async (thumbId) => audioMap[thumbId] || []);
    apiMocks.publishScenario.mockResolvedValue({
        ...scenario,
        visibilityStatus: "PUBLISHED",
    });
    apiMocks.updateScenarioStoryboard.mockImplementation(async (_, body) => ({
        ...scenario,
        storyboardLayoutMode: body.layoutMode,
        storyboardPreset: body.preset,
        storyboardColumns: body.columns,
    }));
    apiMocks.updateThumbnailLayout.mockResolvedValue({});
    apiMocks.uploadScenarioThumbnail.mockResolvedValue({});

    const {wrapper, router} = await mountWithRouter(ScenarioDetailView, {
        routes: [
            {
                path: "/scenarios/:id",
                component: ScenarioDetailView,
                props: true,
            },
        ],
        initialRoute: "/scenarios/77",
        mountOptions: {
            props: {
                id: "77",
            },
        },
    });

    await flushPromises(10);

    return {wrapper, router};
}

describe("ScenarioDetailView", () => {
    beforeEach(() => {
        vi.clearAllMocks();

        autoplayApi.currentIndex.value = -1;
        autoplayApi.currentItem.value = null;
        autoplayApi.currentTime.value = 0;
        autoplayApi.duration.value = 0;
        autoplayApi.autoContinue.value = true;
        autoplayApi.loopScenario.value = false;
    });

    it("loads scenario, language, thumbnails and audios on mount", async () => {
        const {wrapper} = await mountScenarioView();

        expect(apiMocks.loadMe).toHaveBeenCalled();
        expect(apiMocks.fetchScenario).toHaveBeenCalledWith("77");
        expect(apiMocks.fetchLanguage).toHaveBeenCalledWith(42);
        expect(apiMocks.fetchScenarioThumbnails).toHaveBeenCalledWith("77");
        expect(apiMocks.fetchThumbnailAudios).toHaveBeenCalledTimes(2);

        expect(wrapper.text()).toContain("Scenario Alpha");
        expect(wrapper.text()).toContain("Owner view");
        expect(wrapper.find(".audio-panel-thumb").text()).toBe("5");
        expect(wrapper.find(".audio-panel-audios").text()).toBe("2");
    });

    it("falls back to Unknown language when language fetch fails", async () => {
        apiMocks.fetchLanguage.mockRejectedValueOnce(new Error("boom"));
        const {wrapper} = await mountScenarioView();

        expect(wrapper.text()).toContain("Scenario Alpha");

        const infoButton = wrapper.find('button[aria-label="Open scenario information"]');
        await infoButton.trigger("click");
        await flushPromises();
        await nextTick();

        expect(wrapper.text()).toContain("Unknown language");
    });

    it("shows an error alert when initial loading fails", async () => {
        apiMocks.fetchScenario.mockRejectedValueOnce(new Error("Load failed"));

        const {wrapper} = await mountWithRouter(ScenarioDetailView, {
            routes: [
                {
                    path: "/scenarios/:id",
                    component: ScenarioDetailView,
                    props: true,
                },
            ],
            initialRoute: "/scenarios/77",
            mountOptions: {
                props: {id: "77"},
            },
        });

        await flushPromises();

        expect(wrapper.find('.base-alert-stub[data-type="error"]').exists()).toBe(true);
        expect(wrapper.text()).toContain("Load failed");
    });

    it("selects another thumbnail when clicking a thumbnail card", async () => {
        const {wrapper} = await mountScenarioView();

        const buttons = wrapper.findAll(".thumbnail-card-stub");
        expect(buttons.map((b) => b.text())).toEqual(["First", "Second"]);

        await buttons[1].trigger("click");
        await flushPromises();
        await nextTick();

        expect(wrapper.find(".audio-panel-thumb").text()).toBe("10");
        expect(wrapper.find(".audio-panel-audios").text()).toBe("1");
    });

    it("publishes the scenario and updates UI", async () => {
        const {wrapper} = await mountScenarioView();

        const publishButton = wrapper.findAll("button")
            .find((b) => b.text().includes("Publish scenario"));

        expect(publishButton).toBeTruthy();

        await publishButton.trigger("click");
        await flushPromises();

        expect(apiMocks.publishScenario).toHaveBeenCalledWith("77");
        expect(apiMocks.toastSuccess).toHaveBeenCalledWith("Scenario published.");
        expect(wrapper.text()).toContain("PUBLISHED");
    });

    it("saves storyboard settings with normalized numeric columns", async () => {
        const {wrapper} = await mountScenarioView();

        const inputs = wrapper.findAll('input[type="number"]');
        const columnsInput = inputs[0];
        await columnsInput.setValue("99");

        const saveButton = wrapper.findAll("button")
            .find((b) => b.text().includes("Save storyboard settings"));

        await saveButton.trigger("click");
        await flushPromises();

        expect(apiMocks.updateScenarioStoryboard).toHaveBeenCalledWith("77", {
            layoutMode: "PRESET",
            preset: "GRID_3",
            columns: 8,
        });
        expect(apiMocks.toastSuccess).toHaveBeenCalledWith("Storyboard settings saved.");
    });

    it("opens selected layout panel and saves selected thumbnail layout", async () => {
        const {wrapper} = await mountScenarioView();

        const toggleButton = wrapper.findAll("button")
            .find((b) => b.text().includes("Selected thumbnail layout"));

        await toggleButton.trigger("click");
        await flushPromises();
        await nextTick();

        const numberInputs = wrapper.findAll('input[type="number"]');
        const layoutInputs = numberInputs.slice(-4);

        await layoutInputs[0].setValue("4");
        await layoutInputs[1].setValue("");
        await layoutInputs[2].setValue("3");
        await layoutInputs[3].setValue("2");

        const saveButton = wrapper.findAll("button")
            .find((b) => b.text().includes("Save thumbnail layout"));

        await saveButton.trigger("click");
        await flushPromises();

        expect(apiMocks.updateThumbnailLayout).toHaveBeenCalledWith(5, {
            gridColumn: 4,
            gridRow: null,
            gridColumnSpan: 3,
            gridRowSpan: 2,
        });
        expect(apiMocks.toastSuccess).toHaveBeenCalledWith("Thumbnail layout saved.");
    });

    it("opens upload dialog and uploads an image successfully", async () => {
        const {wrapper} = await mountScenarioView();

        const openUploadButton = wrapper.find('button[aria-label="Add a thumbnail"]');
        await openUploadButton.trigger("click");
        await flushPromises();
        await nextTick();

        const fileInput = wrapper.find('input[type="file"][accept="image/*"]');
        const file = new File(["fake-image"], "thumb.png", {type: "image/png"});

        Object.defineProperty(fileInput.element, "files", {
            value: [file],
            configurable: true,
        });

        await fileInput.trigger("change");

        const titleInput = wrapper.find('input[placeholder="Optional image title"]');
        await titleInput.setValue("New thumb");

        const uploadButton = wrapper.findAll("button")
            .find((b) => b.text().includes("Upload image"));

        await uploadButton.trigger("click");
        await flushPromises();

        expect(apiMocks.uploadScenarioThumbnail).toHaveBeenCalledTimes(1);
        expect(apiMocks.fetchScenarioThumbnails).toHaveBeenCalledTimes(2);
        expect(apiMocks.toastSuccess).toHaveBeenCalledWith("Thumbnail uploaded successfully.");
    });

    it("shows upload error if no image is selected", async () => {
        const {wrapper} = await mountScenarioView();

        const openUploadButton = wrapper.find('button[aria-label="Add a thumbnail"]');
        await openUploadButton.trigger("click");
        await flushPromises();
        await nextTick();

        const uploadButton = wrapper.findAll("button")
            .find((b) => b.text().includes("Upload image"));

        await uploadButton.trigger("click");
        await flushPromises();

        expect(apiMocks.uploadScenarioThumbnail).not.toHaveBeenCalled();
        expect(wrapper.text()).toContain("No image selected.");
        expect(apiMocks.toastError).toHaveBeenCalled();
    });

    it("refreshes audios when AudioPanel emits uploaded", async () => {
        const {wrapper} = await mountScenarioView();

        expect(apiMocks.fetchScenarioThumbnails).toHaveBeenCalledTimes(1);

        await wrapper.find(".audio-panel-uploaded").trigger("click");
        await flushPromises();

        expect(apiMocks.fetchScenarioThumbnails).toHaveBeenCalledTimes(2);
    });

    it("delegates play-audio from AudioPanel to autoplay starting at the matching item", async () => {
        const {wrapper} = await mountScenarioView();

        await wrapper.find(".audio-panel-play").trigger("click");
        await flushPromises();

        expect(autoplayApi.playFromIndex).toHaveBeenCalledWith(0);
    });

    it("handles audio fetch failure for a thumbnail without crashing", async () => {
        apiMocks.fetchScenario.mockResolvedValue(baseScenario());
        apiMocks.fetchLanguage.mockResolvedValue({id: 42, name: "Chuj"});
        apiMocks.fetchScenarioThumbnails.mockResolvedValue(baseThumbnails());
        apiMocks.fetchThumbnailAudios
            .mockResolvedValueOnce(audioMapByThumb()[5])
            .mockRejectedValueOnce(new Error("audio load failed"));

        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {
        });

        const {wrapper} = await mountWithRouter(ScenarioDetailView, {
            routes: [
                {
                    path: "/scenarios/:id",
                    component: ScenarioDetailView,
                    props: true,
                },
            ],
            initialRoute: "/scenarios/77",
            mountOptions: {
                props: {id: "77"},
            },
        });

        await flushPromises();

        expect(wrapper.text()).toContain("Scenario Alpha");
        expect(consoleSpy).toHaveBeenCalled();
        consoleSpy.mockRestore();
    });
});