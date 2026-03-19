<script setup>
import {onMounted, ref, watch} from "vue";
import {RouterLink, useRoute, useRouter} from "vue-router";
import {fetchLanguages} from "../api/languages";
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

    const data = await fetchLanguages(params);
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
      q: search.value.trim() || undefined,
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
    <section class="section">
      <div class="section-heading">
        <div>
          <h1>Language catalog</h1>
          <p class="muted">Browse the language inventory used by the platform.</p>
        </div>
      </div>

      <div class="card search-panel">
        <div class="toolbar">
          <input
              v-model="search"
              type="text"
              placeholder="Search languages"
              @keyup.enter="submitSearch"
          />
          <button class="btn btn--primary" @click="submitSearch">Search</button>
        </div>
      </div>

      <p v-if="loading" class="muted">Loading languages...</p>
      <p v-else-if="error" class="error">{{ error }}</p>
      <template v-else>
        <div class="results-meta">
          <span>{{ languages.length }} result(s) on this page</span>
          <span>{{ totalPages > 0 ? `Page ${page + 1} of ${totalPages}` : "No pages" }}</span>
        </div>

        <div v-if="languages.length" class="table-wrap card">
          <table class="table">
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
              <td>{{ l.level ?? "-" }}</td>
              <td>{{ l.family ?? "-" }}</td>
              <td>{{ l.parent ?? "-" }}</td>
            </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="card empty-state">
          <h3>No languages found</h3>
          <p class="muted">Try another query or clear the search field.</p>
        </div>

        <PaginationControls
            :page="page"
            :total-pages="totalPages"
            @go="goToPage"
        />
      </template>
    </section>
  </main>
</template>