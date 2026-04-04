import {defineComponent, h, nextTick, ref} from "vue";
import {mount} from "@vue/test-utils";
import {useScenarioAutoplay} from "@/composables/useScenarioAutoplay";

class FakeAudio {
    constructor() {
        this.src = "";
        this.currentTime = 0;
        this.duration = 0;
        this.preload = "";
        this.ended = false;
        this.seeking = false;
        this._listeners = new Map();
        this.play = vi.fn(async () => {
            this._emit("play");
        });
        this.pause = vi.fn(() => {
            this._emit("pause");
        });
        this.load = vi.fn();
        this.removeAttribute = vi.fn((name) => {
            if (name === "src") {
                this.src = "";
            }
        });
    }

    addEventListener(name, handler) {
        if (!this._listeners.has(name)) {
            this._listeners.set(name, []);
        }
        this._listeners.get(name).push(handler);
    }

    _emit(name) {
        const handlers = this._listeners.get(name) || [];
        for (const handler of handlers) {
            handler();
        }
    }
}

function mountAutoplay(options = {}) {
    const playlist = ref(options.playlist || []);

    const callbacks = {
        onItemChange: vi.fn(),
        onStop: vi.fn(),
        onEndedAll: vi.fn(),
    };

    let api;

    const Host = defineComponent({
        setup() {
            api = useScenarioAutoplay(playlist, {
                gapMs: options.gapMs ?? 10,
                autoContinue: options.autoContinue ?? true,
                loopScenario: options.loopScenario ?? false,
                ...callbacks,
            });
            return () => h("div");
        },
    });

    const wrapper = mount(Host);

    return {
        wrapper,
        playlist,
        api,
        callbacks,
    };
}

describe("useScenarioAutoplay", () => {
    let OriginalAudio;

    beforeEach(() => {
        vi.useFakeTimers();
        OriginalAudio = global.Audio;
        global.Audio = FakeAudio;
    });

    afterEach(() => {
        vi.runOnlyPendingTimers();
        vi.useRealTimers();
        global.Audio = OriginalAudio;
    });

    function samplePlaylist() {
        return [
            {
                thumbnailId: 1,
                audioId: 101,
                audioUrl: "/a1.mp3",
                audioTitle: "Audio 1",
            },
            {
                thumbnailId: 1,
                audioId: 102,
                audioUrl: "/a2.mp3",
                audioTitle: "Audio 2",
            },
            {
                thumbnailId: 2,
                audioId: 201,
                audioUrl: "/a3.mp3",
                audioTitle: "Audio 3",
            },
        ];
    }

    it("plays from a requested index and updates current item", async () => {
        const {api, callbacks} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromIndex(1);

        expect(api.currentIndex.value).toBe(1);
        expect(api.currentItem.value.audioId).toBe(102);
        expect(api.audio.src).toBe("/a2.mp3");
        expect(api.isPlaying.value).toBe(true);
        expect(callbacks.onItemChange).toHaveBeenCalledWith(
            expect.objectContaining({audioId: 102}),
            1
        );
    });

    it("clamps playFromIndex to valid bounds", async () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromIndex(999);
        expect(api.currentIndex.value).toBe(2);

        await api.playFromIndex(-20);
        expect(api.currentIndex.value).toBe(0);
    });

    it("playFromStart starts at index 0", async () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromStart();

        expect(api.currentIndex.value).toBe(0);
        expect(api.currentItem.value.audioId).toBe(101);
    });

    it("resume restarts from start when no audio source exists", async () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.resume();

        expect(api.currentIndex.value).toBe(0);
        expect(api.currentItem.value.audioId).toBe(101);
    });

    it("pause updates state", async () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromStart();
        api.pause();

        expect(api.audio.pause).toHaveBeenCalled();
        expect(api.isPlaying.value).toBe(false);
        expect(api.isPaused.value).toBe(true);
    });

    it("stop fully resets the player and calls onStop", async () => {
        const {api, callbacks} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromStart();
        api.stop();

        expect(api.currentIndex.value).toBe(-1);
        expect(api.currentItem.value).toBeNull();
        expect(api.isPlaying.value).toBe(false);
        expect(api.isPaused.value).toBe(false);
        expect(api.isLoading.value).toBe(false);
        expect(api.currentTime.value).toBe(0);
        expect(api.duration.value).toBe(0);
        expect(api.audio.removeAttribute).toHaveBeenCalledWith("src");
        expect(api.audio.load).toHaveBeenCalled();
        expect(callbacks.onStop).toHaveBeenCalled();
    });

    it("next advances to the next item", async () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromIndex(0);
        await api.next();

        expect(api.currentIndex.value).toBe(1);
        expect(api.currentItem.value.audioId).toBe(102);
    });

    it("next stops and calls onEndedAll at end when loop is disabled", async () => {
        const {api, callbacks} = mountAutoplay({
            playlist: samplePlaylist(),
            loopScenario: false,
        });

        await api.playFromIndex(2);
        await api.next();

        expect(api.currentIndex.value).toBe(-1);
        expect(callbacks.onEndedAll).toHaveBeenCalled();
        expect(callbacks.onStop).toHaveBeenCalled();
    });

    it("next loops to the first item at end when loop is enabled", async () => {
        const {api, callbacks} = mountAutoplay({
            playlist: samplePlaylist(),
            loopScenario: true,
        });

        await api.playFromIndex(2);
        await api.next();

        expect(api.currentIndex.value).toBe(0);
        expect(api.currentItem.value.audioId).toBe(101);
        expect(callbacks.onEndedAll).not.toHaveBeenCalled();
    });

    it("previous replays current item when current time is greater than 3 seconds", async () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromIndex(1);
        api.currentTime.value = 4.2;

        await api.previous();

        expect(api.currentIndex.value).toBe(1);
        expect(api.currentItem.value.audioId).toBe(102);
        expect(api.audio.src).toBe("/a2.mp3");
    });

    it("previous goes to previous item when current time is small", async () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromIndex(2);
        api.currentTime.value = 1.5;

        await api.previous();

        expect(api.currentIndex.value).toBe(1);
        expect(api.currentItem.value.audioId).toBe(102);
    });

    it("seekToPercent updates currentTime proportionally", () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        api.duration.value = 200;
        api.seekToPercent(25);

        expect(api.audio.currentTime).toBe(50);
        expect(api.currentTime.value).toBe(50);
    });

    it("seekToSeconds clamps to duration", () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        api.duration.value = 120;
        api.seekToSeconds(500);

        expect(api.audio.currentTime).toBe(120);
        expect(api.currentTime.value).toBe(120);
    });

    it("formatTime returns m:ss", () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        expect(api.formatTime(0)).toBe("0:00");
        expect(api.formatTime(5)).toBe("0:05");
        expect(api.formatTime(65)).toBe("1:05");
        expect(api.formatTime(600)).toBe("10:00");
    });

    it("loadedmetadata and timeupdate listeners sync duration and currentTime", async () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromStart();

        api.audio.duration = 98;
        api.audio._emit("loadedmetadata");
        expect(api.duration.value).toBe(98);

        api.audio.currentTime = 17.4;
        api.audio._emit("timeupdate");
        expect(api.currentTime.value).toBe(17.4);
    });

    it("ended auto-continues when enabled", async () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
            autoContinue: true,
            gapMs: 25,
        });

        await api.playFromIndex(0);

        api.audio._emit("ended");
        expect(api.currentIndex.value).toBe(0);

        await vi.advanceTimersByTimeAsync(25);

        expect(api.currentIndex.value).toBe(1);
        expect(api.currentItem.value.audioId).toBe(102);
    });

    it("ended does not auto-continue when disabled", async () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
            autoContinue: false,
        });

        await api.playFromIndex(0);

        api.audio._emit("ended");
        await vi.runAllTimersAsync();

        expect(api.currentIndex.value).toBe(0);
        expect(api.isPlaying.value).toBe(false);
        expect(api.isPaused.value).toBe(false);
    });

    it("toggleAutoContinue and toggleLoopScenario flip flags", () => {
        const {api} = mountAutoplay({
            playlist: samplePlaylist(),
            autoContinue: true,
            loopScenario: false,
        });

        api.toggleAutoContinue();
        api.toggleLoopScenario();

        expect(api.autoContinue.value).toBe(false);
        expect(api.loopScenario.value).toBe(true);
    });

    it("stops when playlist becomes empty", async () => {
        const {api, playlist, callbacks} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromIndex(1);
        playlist.value = [];
        await nextTick();

        expect(api.currentIndex.value).toBe(-1);
        expect(callbacks.onStop).toHaveBeenCalled();
    });

    // it("stops when the current item disappears from the playlist", async () => {
    //     const {api, playlist, callbacks} = mountAutoplay({
    //         playlist: samplePlaylist(),
    //     });
    //
    //     await api.playFromIndex(1);
    //
    //     expect(api.currentItem.value).toEqual(
    //         expect.objectContaining({
    //             audioId: 102,
    //             thumbnailId: 1,
    //         })
    //     );
    //
    //     playlist.value = [
    //         {
    //             thumbnailId: 1,
    //             audioId: 101,
    //             audioUrl: "/a1.mp3",
    //         },
    //         {
    //             thumbnailId: 2,
    //             audioId: 201,
    //             audioUrl: "/a3.mp3",
    //         },
    //     ];
    //
    //     await nextTick();
    //     await nextTick();
    //
    //     expect(api.currentIndex.value).toBe(-1);
    //     expect(callbacks.onStop).toHaveBeenCalled();
    // });

    it("stops on unmount", async () => {
        const {api, wrapper, callbacks} = mountAutoplay({
            playlist: samplePlaylist(),
        });

        await api.playFromStart();
        wrapper.unmount();

        expect(callbacks.onStop).toHaveBeenCalled();
    });
});