<script setup>
import {onMounted, ref} from "vue";
import {RouterLink} from "vue-router";
import {apiFetch} from "../api/rest";

const props = defineProps({
  id: {type: String, required: true},
});

const language = ref(null);
const scenarios = ref([]);
const error = ref("");
const loading = ref(false);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    language.value = await apiFetch(`/api/languages/${props.id}`);
    scenarios.value = await apiFetch(`/api/languages/${props.id}/scenarios`);
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <main class="page">
    <p v-if="loading">Loading...</p>
    <p v-else-if="error">{{ error }}</p>

    <template v-else-if="language">
      <h1>Language Details</h1>
      <h2>{{ language.name ?? "-" }}</h2>

      <p>
        <strong>Family:</strong>
        <RouterLink
            v-if="language.familyId"
            :to="`/languages/${language.familyId}`"
        >
          {{ language.familyName }}
        </RouterLink>
        <span v-else>-</span>
      </p>

      <p>
        <strong>Parent:</strong>
        <RouterLink
            v-if="language.parentId"
            :to="`/languages/${language.parentId}`"
        >
          {{ language.parentName }}
        </RouterLink>
        <span v-else>-</span>
      </p>

      <p><strong>Level:</strong> {{ language.level ?? "-" }}</p>
      <p><strong>Description:</strong> {{ language.description ?? "" }}</p>

      <h2>Related Scenarios</h2>
      <p>{{ scenarios.length }} scenario(s) using this language</p>

      <table class="table">
        <thead>
        <tr>
          <th>Name</th>
          <th>Author</th>
          <th>Creation Date</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="s in scenarios" :key="s.id">
          <td>
            <RouterLink :to="`/scenarios/${s.id}`">
              {{ s.title ?? "Untitled scenario" }}
            </RouterLink>
          </td>
          <td>{{ s.authorUsername ?? "Unknown author" }}</td>
          <td>{{ s.createdAt ?? "Unknown date" }}</td>
        </tr>
        </tbody>
      </table>
    </template>
  </main>
</template>