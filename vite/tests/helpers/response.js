export function jsonResponse(body, status = 200, headers = {}) {
    return new Response(JSON.stringify(body), {
        status,
        headers: {
            "Content-Type": "application/json",
            ...headers,
        },
    });
}

export function textResponse(body, status = 200, headers = {}) {
    return new Response(body, {
        status,
        headers,
    });
}