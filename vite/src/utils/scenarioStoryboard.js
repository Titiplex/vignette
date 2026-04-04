export function safeNumber(value, fallback = 0) {
    const n = Number(value);
    return Number.isFinite(n) ? n : fallback;
}

export function clamp(value, min, max) {
    return Math.max(min, Math.min(max, value));
}

export function nullableInt(value) {
    if (value === "" || value === null || value === undefined) return null;
    const n = Number(value);
    return Number.isFinite(n) ? Math.trunc(n) : null;
}

export function normalizeMarkers(audios) {
    return (audios || [])
        .filter((audio) =>
            audio?.markerX !== null &&
            audio?.markerX !== undefined &&
            audio?.markerY !== null &&
            audio?.markerY !== undefined &&
            audio?.markerX !== "" &&
            audio?.markerY !== ""
        )
        .map((audio) => {
            const x = Number(audio.markerX);
            const y = Number(audio.markerY);

            return {
                ...audio,
                _x: Number.isFinite(x) ? clamp(x, 0, 100) : null,
                _y: Number.isFinite(y) ? clamp(y, 0, 100) : null,
            };
        })
        .filter((audio) => audio._x !== null && audio._y !== null);
}

export function sortByIdxThenId(items) {
    return [...(items || [])].sort((a, b) => {
        const aIdx = safeNumber(a?.idx, Number.MAX_SAFE_INTEGER);
        const bIdx = safeNumber(b?.idx, Number.MAX_SAFE_INTEGER);
        if (aIdx !== bIdx) return aIdx - bIdx;
        return safeNumber(a?.id, 0) - safeNumber(b?.id, 0);
    });
}

export function buildSelectedAudios(audioMap, selectedThumb) {
    if (!selectedThumb) return [];
    return sortByIdxThenId(audioMap?.[selectedThumb.id] || []);
}

export function buildPlaybackQueue(thumbnails, audioMap, buildAudioUrl) {
    return sortByIdxThenId(thumbnails).flatMap((thumb) => {
        const audios = sortByIdxThenId(audioMap?.[thumb.id] || []);

        return audios.map((audio) => ({
            thumbnailId: thumb.id,
            thumbnailIdx: thumb.idx ?? null,
            thumbnailTitle: thumb.title ?? "",
            audioId: audio.id,
            audioIdx: audio.idx ?? null,
            audioTitle: audio.title ?? "",
            audioUrl: buildAudioUrl(audio),
            markerX: audio.markerX,
            markerY: audio.markerY,
            markerLabel: audio.markerLabel,
        }));
    });
}

export function defaultTileLayout(thumb, index, columns) {
    const width = safeNumber(thumb?.imageWidth, 0);
    const height = safeNumber(thumb?.imageHeight, 0);

    const portrait = height > width * 1.15;
    const landscape = width > height * 1.2;

    if (index === 0) {
        return {
            columnStart: null,
            columnSpan: Math.min(columns, 2),
            rowStart: null,
            rowSpan: landscape ? 2 : 1,
        };
    }

    if (portrait) {
        return {
            columnStart: null,
            columnSpan: 1,
            rowStart: null,
            rowSpan: 2,
        };
    }

    if (landscape) {
        return {
            columnStart: null,
            columnSpan: Math.min(columns, 2),
            rowStart: null,
            rowSpan: 1,
        };
    }

    return {
        columnStart: null,
        columnSpan: 1,
        rowStart: null,
        rowSpan: 1,
    };
}

export function buildStoryboardItems({
                                         thumbnails,
                                         layoutMode,
                                         columns,
                                     }) {
    const normalizedMode = String(layoutMode || "PRESET").toUpperCase();

    return sortByIdxThenId(thumbnails).map((thumb, index) => {
        if (normalizedMode === "CUSTOM") {
            return {
                ...thumb,
                _layout: {
                    columnStart: thumb.gridColumn ?? null,
                    columnSpan: thumb.gridColumnSpan ?? 1,
                    rowStart: thumb.gridRow ?? null,
                    rowSpan: thumb.gridRowSpan ?? 1,
                },
            };
        }

        return {
            ...thumb,
            _layout: defaultTileLayout(thumb, index, columns),
        };
    });
}

export function storyboardItemStyle(item) {
    const layout = item?._layout ?? {
        columnStart: null,
        columnSpan: 1,
        rowStart: null,
        rowSpan: 1,
    };

    return {
        gridColumn: layout.columnStart
            ? `${layout.columnStart} / span ${layout.columnSpan}`
            : `span ${layout.columnSpan}`,
        ...(layout.rowStart
            ? {gridRow: `${layout.rowStart} / span ${layout.rowSpan}`}
            : (layout.rowSpan > 1 ? {gridRow: `span ${layout.rowSpan}`} : {})),
    };
}