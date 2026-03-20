<script setup>
import {computed, onMounted, ref, watch} from "vue";
import {RouterLink, useRoute, useRouter} from "vue-router";
import {useAuth} from "../composables/useAuth";

const route = useRoute();
const router = useRouter();
const {currentUser, isAuthenticated, loadMe, logout} = useAuth();

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

const navItems = computed(() => [
  {to: "/", label: "Home"},
  {to: "/languages", label: "Languages"},
  {to: "/scenarios", label: "Scenarios"},
]);

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
              class="btn btn--ghost"
          >
            {{ currentUser?.username || "My profile" }}
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