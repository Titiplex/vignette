<script setup>
import {ref} from "vue";
import {useRouter} from "vue-router";
import {register} from "../api/auth";

const router = useRouter();

const form = ref({
  username: "",
  email: "",
  password: "",
});

const error = ref("");
const loading = ref(false);

async function submit() {
  loading.value = true;
  error.value = "";
  try {
    await register({
      username: form.value.username.trim(),
      email: form.value.email.trim(),
      password: form.value.password,
    });
    router.push("/login");
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="page auth-page">
    <h1>Create your account</h1>

    <form @submit.prevent="submit" class="form-card">
      <label>
        Username
        <input v-model="form.username"/>
      </label>

      <label>
        Email
        <input v-model="form.email" type="email"/>
      </label>

      <label>
        Password
        <input v-model="form.password" type="password"/>
      </label>

      <button type="submit" :disabled="loading">
        {{ loading ? "Creating..." : "Create account" }}
      </button>

      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </main>
</template>