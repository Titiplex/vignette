<script setup>
import {ref} from "vue";
import {RouterLink, useRoute, useRouter} from "vue-router";
import {useAuth} from "../composables/useAuth";

const router = useRouter();
const route = useRoute();
const {login} = useAuth();

const username = ref("");
const password = ref("");
const error = ref("");
const loading = ref(false);

async function submit() {
  loading.value = true;
  error.value = "";

  try {
    await login(username.value.trim(), password.value);
    router.push(route.query.redirect || "/");
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="page auth-page">
    <section class="form-card auth-card">
      <div class="section-heading">
        <div>
          <h1>Login</h1>
          <p class="muted">Access your scenarios and profile.</p>
        </div>
      </div>

      <form @submit.prevent="submit" class="form-stack">
        <label>
          Username
          <input v-model="username" name="username" autocomplete="username"/>
        </label>

        <label>
          Password
          <input
              v-model="password"
              type="password"
              name="password"
              autocomplete="current-password"
          />
        </label>

        <button :disabled="loading" type="submit" class="btn btn--primary">
          {{ loading ? "Logging in..." : "Login" }}
        </button>

        <p v-if="error" class="error">{{ error }}</p>

        <p class="muted">
          Don’t have an account?
          <RouterLink to="/register">Create one here</RouterLink>
        </p>
      </form>
    </section>
  </main>
</template>