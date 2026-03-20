<script setup>
import {onMounted, ref, watch} from "vue";
import {RouterLink, useRoute, useRouter} from "vue-router";
import {fetchLanguages} from "../api/languages";
import PaginationControls from "../components/PaginationControls.vue";
import BasePageHeader from "../components/ui/BasePageHeader.vue";
import BaseLoader from "../components/ui/BaseLoader.vue";
import BaseAlert from "../components/ui/BaseAlert.vue";
import BaseEmptyState from "../components/ui/BaseEmptyState.vue";
import BaseBadge from "../components/ui/BaseBadge.vue";
import {useDebouncedRef} from "../composables/useDebouncedRef";

const route = useRoute();
const router = useRouter();

const languages = ref([]);
const loading = ref(false);
const error = ref("");
const page = ref(Number(route.query.page ?? 0));
const totalPages = ref(0);

const {source: search, debounced} = useDebouncedRef(route.query.q ?? "", 350);

function levelVariant(level) {
  if (!level) return "neutral";
  const l = String(level).toLowerCase();
  if (l.includes("family")) return "info";
  if (l.includes("language")) return "success";
  if (l.includes("dialect")) return "warning";
  return "neutral";
}

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

function updateRouteSearch(queryValue) {
  router.push({
    path: "/languages",
    query: {
      q: queryValue.trim() || undefined,
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

watch(debounced, (value) => {
  if ((route.query.q ?? "") !== value) {
    updateRouteSearch(value);
  }
});

watch(() => route.fullPath, load);
onMounted(load);
</script>

<template>
  <main class="page">
    <section class="section">
      <BasePageHeader
          title="Language catalog"
          subtitle="Browse the language inventory used by the platform."
      />

      <div class="card search-panel">
        <div class="toolbar">
          <input
              v-model="search"
              type="text"
              placeholder="Search languages"
          />
        </div>
      </div>

      <BaseLoader v-if="loading">Loading languages...</BaseLoader>

      <BaseAlert v-else-if="error" type="error">
        {{ error }}
      </BaseAlert>

      <template v-else>
        <div class="results-meta">
          <span>{{ languages.length }} result(s) on this page</span>
          <span>{{ totalPages > 0 ? `Page ${page + 1} of ${totalPages}` : "No pages" }}</span>
        </div>

        <div v-if="languages.length" class="table-wrap card table-card">
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
              <td class="table__mono">{{ l.id }}</td>
              <td>
                <RouterLink :to="`/languages/${l.id}`" class="table__primary-link">
                  {{ l.name ?? "" }}
                </RouterLink>
              </td>
              <td>
                <BaseBadge :variant="levelVariant(l.level)">
                  {{ l.level ?? "-" }}
                </BaseBadge>
              </td>
              <td>{{ l.family ?? "-" }}</td>
              <td>{{ l.parent ?? "-" }}</td>
            </tr>
            </tbody>
          </table>
        </div>

        <BaseEmptyState
            v-else
            title="No languages found"
            message="Try another search query."
        />

        <PaginationControls
            :page="page"
            :total-pages="totalPages"
            @go="goToPage"
        />
      </template>
    </section>
  </main>
</template>