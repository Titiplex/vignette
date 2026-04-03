export function mockElementRect(element, rect) {
    element.getBoundingClientRect = () => ({
        left: rect.left ?? 0,
        top: rect.top ?? 0,
        width: rect.width ?? 100,
        height: rect.height ?? 100,
        right: rect.right ?? ((rect.left ?? 0) + (rect.width ?? 100)),
        bottom: rect.bottom ?? ((rect.top ?? 0) + (rect.height ?? 100)),
    });
}