<script setup>
import {computed, onMounted, ref, watch} from "vue";
import {RouterLink, useRoute, useRouter} from "vue-router";
import {useAuth} from "../composables/useAuth";

const route = useRoute();
const router = useRouter();
const {currentUser, isAuthenticated, isAdmin, loadMe, logout} = useAuth();

const mobileMenuOpen = ref(false);

onMounted(() => {
  loadMe();
});

watch(
    () => route.fullPath,
    () => {
      mobileMenuOpen.value = false;
    }
);

const navItems = computed(() => {
  const base = [
    {to: "/", label: "Home"},
    {to: "/languages", label: "Languages"},
    {to: "/scenarios", label: "Scenarios"},
    {to: "/about", label: "About"},
  ];

  if (isAuthenticated.value) {
    base.splice(3, 0, {to: "/workspace", label: "Workspace"});
  }

  if (isAdmin.value) {
    base.splice(4, 0, {to: "/admin", label: "Admin"});
  }

  return base;
});

function isActive(path) {
  if (path === "/") return route.path === "/";
  return route.path.startsWith(path);
}

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value;
}

async function doLogout() {
  await logout();
  router.push("/login");
}
</script>

<template>
  <header class="app-header">
    <div class="app-container app-header__inner">
      <RouterLink to="/" class="app-brand">
        <span class="app-brand__title">Vignette</span>
        <span class="app-brand__subtitle">
          Multimedia scenarios for language documentation
        </span>
      </RouterLink>

      <button
          type="button"
          class="app-header__burger"
          :aria-expanded="mobileMenuOpen ? 'true' : 'false'"
          aria-label="Toggle navigation"
          @click="toggleMobileMenu"
      >
        <span></span>
        <span></span>
        <span></span>
      </button>

      <div
          class="app-header__panel"
          :class="{ 'is-open': mobileMenuOpen }"
      >
        <nav class="app-nav" aria-label="Primary">
          <RouterLink
              v-for="item in navItems"
              :key="item.to"
              :to="item.to"
              class="app-nav__link"
              :class="{ 'is-active': isActive(item.to) }"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="app-header__actions">
          <RouterLink
              v-if="isAuthenticated"
              to="/create-scenario"
              class="btn btn--primary"
          >
            Create scenario
          </RouterLink>

          <RouterLink
              v-if="isAuthenticated"
              to="/user"
              class="btn btn--ghost btn--profile"
              :class="{ 'is-active': isActive('/user') }"
              aria-label="Open my profile"
          >
            <span class="btn__icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"
                   stroke-linejoin="round">
                <path d="M20 21a8 8 0 0 0-16 0"/>
                <circle cx="12" cy="8" r="4"/>
              </svg>
            </span>
            <span class="btn__profile-text">
              <span class="btn__profile-label">Me</span>
              <span class="btn__profile-name">{{ currentUser?.username || "My profile" }}</span>
            </span>
          </RouterLink>

          <RouterLink
              v-if="!isAuthenticated"
              to="/login"
              class="btn btn--ghost"
          >
            Login
          </RouterLink>

          <button
              v-else
              type="button"
              class="btn btn--ghost"
              @click="doLogout"
          >
            Logout
          </button>
        </div>
      </div>
    </div>
  </header>
</template>