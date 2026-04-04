import {expect, test} from "@playwright/test";

test("scenario page loads, selects thumbnails, and opens audio workspace", async ({page}) => {
    await page.route("**/api/auth/me", async (route) => {
        await route.fulfill({
            status: 200,
            contentType: "application/json",
            body: JSON.stringify({
                id: 1,
                username: "ownerUser",
            }),
        });
    });

    await page.route("**/api/scenarios/77", async (route) => {
        await route.fulfill({
            status: 200,
            contentType: "application/json",
            body: JSON.stringify({
                id: 77,
                title: "Scenario Alpha",
                description: "Scenario description",
                authorUsername: "ownerUser",
                languageId: 42,
                visibilityStatus: "DRAFT",
                storyboardLayoutMode: "PRESET",
                storyboardPreset: "GRID_3",
                storyboardColumns: 3,
            }),
        });
    });

    await page.route("**/api/languages/42", async (route) => {
        await route.fulfill({
            status: 200,
            contentType: "application/json",
            body: JSON.stringify({
                id: 42,
                name: "Chuj",
            }),
        });
    });

    await page.route("**/api/scenarios/77/thumbnails", async (route) => {
        await route.fulfill({
            status: 200,
            contentType: "application/json",
            body: JSON.stringify([
                {
                    id: 5,
                    idx: 1,
                    title: "First",
                    imageWidth: 800,
                    imageHeight: 1200,
                },
                {
                    id: 10,
                    idx: 2,
                    title: "Second",
                    imageWidth: 1200,
                    imageHeight: 800,
                },
            ]),
        });
    });

    await page.route("**/api/thumbnails/5/audios", async (route) => {
        await route.fulfill({
            status: 200,
            contentType: "application/json",
            body: JSON.stringify([
                {
                    id: 500,
                    idx: 1,
                    title: "Audio A",
                    markerX: 20,
                    markerY: 30,
                    markerLabel: "A",
                },
            ]),
        });
    });

    await page.route("**/api/thumbnails/10/audios", async (route) => {
        await route.fulfill({
            status: 200,
            contentType: "application/json",
            body: JSON.stringify([
                {
                    id: 1000,
                    idx: 1,
                    title: "Audio B",
                    markerX: null,
                    markerY: null,
                    markerLabel: null,
                },
            ]),
        });
    });

    await page.route("**/api/thumbnails/*/content", async (route) => {
        await route.fulfill({
            status: 200,
            contentType: "image/png",
            body: "",
        });
    });

    await page.goto("/scenarios/77");

    await expect(page.getByText("Scenario Alpha")).toBeVisible();
    await expect(page.getByText("Owner view")).toBeVisible();

    await page.getByRole("button", {name: /selected thumbnail/i}).click();
    await expect(page.getByText(/First/)).toBeVisible();

    await page.getByRole("button", {name: /audio workspace/i}).click();
    await expect(page.getByText("Existing audio clips")).toBeVisible();

    await page.getByRole("button", {name: "Play"}).first().click();
});