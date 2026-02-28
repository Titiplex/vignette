import {apiFetch, setAccessToken} from "../api/rest.js";
import {updateHeaderAuth} from "../api/header.js";

updateHeaderAuth().then(() => {
});

// Restore JWT token from sessionStorage
const token = sessionStorage.getItem("accessToken");
if (token) {
    setAccessToken(token);
}

const params = new URLSearchParams(window.location.search);
const publicUserId = params.get("id");

const privateSection = document.getElementById("privateProfileSection");
const publicSection = document.getElementById("publicProfileSection");
const title = document.getElementById("profileTitle");
const subtitle = document.getElementById("profileSubtitle");

if (publicUserId) {
    loadPublicProfile(publicUserId).then(() => {});
} else {
    loadMyProfile().then(() => {});
}

async function loadMyProfile() {
    try {
        const me = await apiFetch("/api/users/me/profile");
        privateSection.style.display = "block";
        const form = document.getElementById("profileForm");
        form.displayName.value = me.displayName || "";
        form.academyAffiliations.value = (me.academyAffiliations || []).join("\n");
        form.profilePublic.checked = !!me.profilePublic;

        document.getElementById("roleLine").textContent = `Roles: ${(me.roles || []).join(", ")}`;
        updatePublicLink(me.id, me.profilePublic);

        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const saveMessage = document.getElementById("saveMessage");
            saveMessage.textContent = "Saving...";
            try {
                const payload = {
                    displayName: form.displayName.value,
                    institution: form.institution.value,
                    researchInterests: form.researchInterests.value,
                    bio: form.bio.value,
                    profilePublic: form.profilePublic.checked,
                    academyAffiliations: form.academyAffiliations.value
                        .split("\n")
                        .map(v => v.trim())
                        .filter(Boolean)
                };
                const updated = await apiFetch("/api/users/me/profile", {method: "PUT", body: payload});
                saveMessage.textContent = "Profile saved.";
                updatePublicLink(updated.id, updated.profilePublic);
            } catch (err) {
                saveMessage.textContent = err.message;
            }
        });
    } catch (_) {
        window.location.href = "/pages/login.html";
    }
}

function updatePublicLink(id, isPublic) {
    const linkLine = document.getElementById("publicLinkLine");
    if (!isPublic) {
        linkLine.textContent = "Your profile is currently private.";
        return;
    }
    const path = `/pages/user.html?id=${id}`;
    linkLine.innerHTML = `Public profile link: <a href="${path}">${window.location.origin}${path}</a>`;
}

async function loadPublicProfile(userId) {
    try {
        const profile = await apiFetch(`/api/users/${userId}/profile`);
        subtitle.textContent = "Public profile";
        publicSection.style.display = "block";

        document.getElementById("publicName").textContent = profile.displayName || profile.username;
        document.getElementById("publicInstitution").textContent = profile.institution || "";
        document.getElementById("publicInterests").textContent = profile.researchInterests || "";
        document.getElementById("publicBio").textContent = profile.bio || "";

        const roles = document.getElementById("publicRoles");
        roles.innerHTML = "";
        (profile.roles || []).forEach((role) => {
            const li = document.createElement("li");
            li.textContent = role;
            roles.appendChild(li);
        });

        const affiliations = document.getElementById("publicAffiliations");
        affiliations.innerHTML = "";
        (profile.academyAffiliations || []).forEach((entry) => {
            const li = document.createElement("li");
            li.textContent = entry;
            affiliations.appendChild(li);
        });
    } catch (err) {
        title.textContent = "Profile unavailable";
        subtitle.textContent = err.message;
    }
}
