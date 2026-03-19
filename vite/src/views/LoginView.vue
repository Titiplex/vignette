<script setup>
import {ref} from "vue";
import {RouterLink, useRouter} from "vue-router";
import {useAuth} from "../composables/useAuth";

const router = useRouter();
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
    router.push("/");
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="page auth-page">
    <h1>Login</h1>

    <form @submit.prevent="submit" class="form-card">
      <label>
        Username
        <input v-model="username" name="username"/>
      </label>

      <label>
        Password
        <input v-model="password" type="password" name="password"/>
      </label>

      <button :disabled="loading" type="submit">
        {{ loading ? "Logging in..." : "Login" }}
      </button>

      <p v-if="error" class="error">{{ error }}</p>
      <p>
        Don't have an account?
        <RouterLink to="/register">Register here</RouterLink>
      </p>
    </form>
  </main>
</template>