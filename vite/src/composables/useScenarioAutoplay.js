import {computed, onBeforeUnmount, ref, watch} from "vue";

export function useScenarioAutoplay(playlistRef, options = {}) {
    const gapMs = options.gapMs ?? 320;
    const onItemChange = options.onItemChange ?? (() => {
    });
    const onStop = options.onStop ?? (() => {
    });
    const onEndedAll = options.onEndedAll ?? (() => {
    });

    const audio = new Audio();
    audio.preload = "auto";

    const currentIndex = ref(-1);
    const isPlaying = ref(false);
    const isPaused = ref(false);
    const isLoading = ref(false);

    const currentTime = ref(0);
    const duration = ref(0);

    const currentItem = computed(() => {
        if (currentIndex.value < 0) return null;
        return playlistRef.value[currentIndex.value] ?? null;
    });

    const progressPercent = computed(() => {
        if (!duration.value || duration.value <= 0) return 0;
        return Math.max(0, Math.min(100, (currentTime.value / duration.value) * 100));
    });

    let nextTimeout = null;

    function clearPendingNext() {
        if (nextTimeout) {
            clearTimeout(nextTimeout);
            nextTimeout = null;
        }
    }

    function resetTimeState() {
        currentTime.value = 0;
        duration.value = 0;
    }

    async function playIndex(index) {
        const item = playlistRef.value[index];
        if (!item?.audioUrl) return;

        clearPendingNext();
        isLoading.value = true;

        try {
            audio.pause();
            audio.src = item.audioUrl;
            audio.currentTime = 0;
            resetTimeState();

            currentIndex.value = index;
            onItemChange(item, index);

            await audio.play();

            isPlaying.value = true;
            isPaused.value = false;
        } finally {
            isLoading.value = false;
        }
    }

    async function playFromStart() {
        if (!playlistRef.value.length) return;
        await playIndex(0);
    }

    async function playFromIndex(index) {
        if (!playlistRef.value.length) return;
        const safeIndex = Math.max(0, Math.min(index, playlistRef.value.length - 1));
        await playIndex(safeIndex);
    }

    async function resume() {
        if (!audio.src) {
            await playFromStart();
            return;
        }

        clearPendingNext();
        await audio.play();
        isPlaying.value = true;
        isPaused.value = false;
    }

    function pause() {
        clearPendingNext();
        audio.pause();
        isPlaying.value = false;
        isPaused.value = true;
    }

    function stop() {
        clearPendingNext();
        audio.pause();
        audio.currentTime = 0;
        audio.removeAttribute("src");
        audio.load();

        currentIndex.value = -1;
        isPlaying.value = false;
        isPaused.value = false;
        isLoading.value = false;
        resetTimeState();

        onStop();
    }

    async function next() {
        const nextIndex = currentIndex.value + 1;

        if (nextIndex >= playlistRef.value.length) {
            stop();
            onEndedAll();
            return;
        }

        await playIndex(nextIndex);
    }

    async function previous() {
        if (!playlistRef.value.length) return;

        const prevIndex = currentIndex.value <= 0 ? 0 : currentIndex.value - 1;
        await playIndex(prevIndex);
    }

    function seekToPercent(percent) {
        if (!duration.value || duration.value <= 0) return;
        const clamped = Math.max(0, Math.min(100, Number(percent) || 0));
        audio.currentTime = (clamped / 100) * duration.value;
        currentTime.value = audio.currentTime;
    }

    function seekToSeconds(seconds) {
        if (!duration.value || duration.value <= 0) return;
        const clamped = Math.max(0, Math.min(duration.value, Number(seconds) || 0));
        audio.currentTime = clamped;
        currentTime.value = clamped;
    }

    function formatTime(totalSeconds) {
        const secs = Math.max(0, Math.floor(Number(totalSeconds) || 0));
        const minutes = Math.floor(secs / 60);
        const seconds = secs % 60;
        return `${minutes}:${String(seconds).padStart(2, "0")}`;
    }

    audio.addEventListener("loadedmetadata", () => {
        duration.value = Number.isFinite(audio.duration) ? audio.duration : 0;
    });

    audio.addEventListener("timeupdate", () => {
        currentTime.value = Number.isFinite(audio.currentTime) ? audio.currentTime : 0;
    });

    audio.addEventListener("ended", () => {
        clearPendingNext();
        nextTimeout = setTimeout(() => {
            next().catch(() => {
                stop();
            });
        }, gapMs);
    });

    audio.addEventListener("pause", () => {
        if (!audio.ended && !audio.seeking && audio.src) {
            isPlaying.value = false;
        }
    });

    audio.addEventListener("play", () => {
        isPlaying.value = true;
        isPaused.value = false;
    });

    watch(
        playlistRef,
        (list) => {
            if (!list.length) {
                stop();
                return;
            }

            if (currentIndex.value >= list.length) {
                stop();
                return;
            }

            if (currentIndex.value >= 0) {
                const current = currentItem.value;
                const sameItemStillExists = list.some(
                    (item) =>
                        String(item.audioId) === String(current?.audioId) &&
                        String(item.thumbnailId) === String(current?.thumbnailId)
                );

                if (!sameItemStillExists) {
                    stop();
                }
            }
        },
        {deep: true}
    );

    onBeforeUnmount(() => {
        stop();
    });

    return {
        audio,
        currentIndex,
        currentItem,
        currentTime,
        duration,
        progressPercent,
        isPlaying,
        isPaused,
        isLoading,
        playFromStart,
        playFromIndex,
        resume,
        pause,
        stop,
        next,
        previous,
        seekToPercent,
        seekToSeconds,
        formatTime,
    };
}