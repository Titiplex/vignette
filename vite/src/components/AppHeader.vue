<script setup>
import {onMounted} from "vue";
import {RouterLink, useRouter} from "vue-router";
import {useAuth} from "../composables/useAuth";

const router = useRouter();
const {currentUser, isAuthenticated, loadMe, logout} = useAuth();

onMounted(loadMe);

async function doLogout() {
  await logout();
  router.push("/login");
}
</script>

<template>
  <header class="main-header sticky-header">
    <nav>
      <ul class="nav-list">
        <li>
          <RouterLink to="/">Home</RouterLink>
        </li>
        <li>
          <RouterLink to="/languages">Languages</RouterLink>
        </li>
        <li>
          <RouterLink to="/scenarios">Scenarios</RouterLink>
        </li>
        <li v-if="isAuthenticated">
          <RouterLink to="/create-scenario">Create scenario</RouterLink>
        </li>
        <li v-if="isAuthenticated">
          <RouterLink to="/user">My profile</RouterLink>
        </li>
        <li v-if="!isAuthenticated">
          <RouterLink to="/login">Login</RouterLink>
        </li>
        <li v-else>
          <a href="#" @click.prevent="doLogout">Logout</a>
        </li>
      </ul>
    </nav>
  </header>
</template>