<script setup>
import {onMounted, ref, watch} from "vue";
import {RouterLink, useRoute, useRouter} from "vue-router";
import {apiFetch} from "../api/rest";
import PaginationControls from "../components/PaginationControls.vue";

const route = useRoute();
const router = useRouter();

const languages = ref([]);
const loading = ref(false);
const error = ref("");
const search = ref(route.query.q ?? "");
const page = ref(Number(route.query.page ?? 0));
const totalPages = ref(0);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    page.value = Number(route.query.page ?? 0);
    search.value = route.query.q ?? "";

    const params = new URLSearchParams({
      page: String(page.value),
      size: "50",
    });

    if (search.value.trim()) {
      params.set("q", search.value.trim());
    }

    const data = await apiFetch(`/api/languages?${params.toString()}`);
    languages.value = data.content ?? [];
    totalPages.value = data.totalPages ?? 0;
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}

function submitSearch() {
  router.push({
    path: "/languages",
    query: {
      q: search.value || undefined,
      page: 0,
    },
  });
}

function goToPage(nextPage) {
  router.push({
    path: "/languages",
    query: {
      ...route.query,
      page: nextPage,
    },
  });
}

watch(() => route.fullPath, load);
onMounted(load);
</script>

<template>
  <main class="page">
    <h1>Language Catalog</h1>
    <p><small>Data from Glottolog, 2026</small></p>

    <div class="toolbar">
      <input
          v-model="search"
          type="text"
          placeholder="Search languages"
          @keyup.enter="submitSearch"
      />
      <button @click="submitSearch">Search</button>
    </div>

    <p v-if="loading">Loading...</p>
    <p v-else-if="error">{{ error }}</p>
    <p v-else>
      {{ totalPages > 0 ? `Page ${page + 1} / ${totalPages}` : "No pages" }}
    </p>

    <table v-if="!loading && !error" class="table">
      <thead>
      <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Level</th>
        <th>Family</th>
        <th>Parent</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="l in languages" :key="l.id">
        <td>{{ l.id }}</td>
        <td>
          <RouterLink :to="`/languages/${l.id}`">
            {{ l.name ?? "" }}
          </RouterLink>
        </td>
        <td>{{ l.level ?? "" }}</td>
        <td>{{ l.family ?? "-" }}</td>
        <td>{{ l.parent ?? "-" }}</td>
      </tr>
      </tbody>
    </table>

    <PaginationControls
        :page="page"
        :total-pages="totalPages"
        @go="goToPage"
    />
  </main>
</template>