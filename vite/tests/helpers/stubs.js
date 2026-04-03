export function simpleComponentStub(name = "Stub") {
    return {
        name,
        template: `<div>${name}</div>`,
    };
}