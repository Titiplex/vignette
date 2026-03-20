<script setup>
import {computed, ref} from "vue";
import {RouterLink, useRoute, useRouter} from "vue-router";
import {useAuth} from "../composables/useAuth";
import BaseAlert from "../components/ui/BaseAlert.vue";
import {useToast} from "../composables/useToast";

const router = useRouter();
const route = useRoute();
const {login} = useAuth();
const toast = useToast();

const username = ref("");
const password = ref("");
const error = ref("");
const loading = ref(false);

const usernameError = computed(() => {
  if (!username.value) return "Username is required.";
  return "";
});

const passwordError = computed(() => {
  if (!password.value) return "Password is required.";
  if (password.value.length < 4) return "Password is too short.";
  return "";
});

const isFormValid = computed(() => {
  return !usernameError.value && !passwordError.value;
});

async function submit() {
  if (!isFormValid.value) {
    error.value = usernameError.value || passwordError.value;
    return;
  }

  loading.value = true;
  error.value = "";

  try {
    await login(username.value.trim(), password.value);
    toast.success("Logged in successfully.");
    router.push(route.query.redirect || "/");
  } catch (e) {
    error.value = e.message;
    toast.error(e.message || "Login failed.");
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="page auth-page">
    <section class="form-card auth-card auth-card--premium">
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
          <span v-if="username && usernameError" class="field-error">
            {{ usernameError }}
          </span>
        </label>

        <label>
          Password
          <input
              v-model="password"
              type="password"
              name="password"
              autocomplete="current-password"
          />
          <span v-if="password && passwordError" class="field-error">
            {{ passwordError }}
          </span>
        </label>

        <button :disabled="loading || !isFormValid" type="submit" class="btn btn--primary">
          {{ loading ? "Logging in..." : "Login" }}
        </button>

        <BaseAlert v-if="error" type="error">
          {{ error }}
        </BaseAlert>

        <p class="muted">
          Don’t have an account?
          <RouterLink to="/register">Create one here</RouterLink>
        </p>
      </form>
    </section>
  </main>
</template>