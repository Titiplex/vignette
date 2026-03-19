<script setup>
import {ref} from "vue";
import {RouterLink, useRouter} from "vue-router";
import {register} from "../api/auth";

const router = useRouter();

const form = ref({
  username: "",
  email: "",
  password: "",
});

const error = ref("");
const loading = ref(false);
const success = ref("");

async function submit() {
  loading.value = true;
  error.value = "";
  success.value = "";

  try {
    await register({
      username: form.value.username.trim(),
      email: form.value.email.trim(),
      password: form.value.password,
    });

    success.value = "Account created successfully. You can now log in.";
    setTimeout(() => {
      router.push("/login");
    }, 700);
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
          <h1>Create your account</h1>
          <p class="muted">Set up your Vignette researcher profile.</p>
        </div>
      </div>

      <form @submit.prevent="submit" class="form-stack">
        <label>
          Username
          <input v-model="form.username" autocomplete="username"/>
        </label>

        <label>
          Email
          <input v-model="form.email" type="email" autocomplete="email"/>
        </label>

        <label>
          Password
          <input v-model="form.password" type="password" autocomplete="new-password"/>
        </label>

        <button type="submit" :disabled="loading" class="btn btn--primary">
          {{ loading ? "Creating..." : "Create account" }}
        </button>

        <p v-if="success" class="success">{{ success }}</p>
        <p v-if="error" class="error">{{ error }}</p>

        <p class="muted">
          Already registered?
          <RouterLink to="/login">Go to login</RouterLink>
        </p>
      </form>
    </section>
  </main>
</template>