<script setup>
import {computed, onMounted, ref} from "vue";
import {fetchAdminUsers, updateAdminUserRoles} from "../api/admin";

const loading = ref(false);
const savingUserId = ref(null);
const error = ref("");
const success = ref("");
const users = ref([]);
const query = ref("");

const editableRoles = [
  "ROLE_USER",
  "ROLE_ADMIN",
];

const filteredUsers = computed(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return users.value;

  return users.value.filter((user) => {
    return [
      user.username,
      user.displayName,
      user.email,
      ...(user.roles ?? []),
    ]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(q));
  });
});

async function loadUsers() {
  loading.value = true;
  error.value = "";
  success.value = "";

  try {
    const rows = await fetchAdminUsers();
    users.value = rows.map((user) => ({
      ...user,
      draftRoles: [...(user.roles ?? [])],
    }));
  } catch (e) {
    error.value = e.message || "Failed to load users.";
  } finally {
    loading.value = false;
  }
}

function toggleRole(user, role) {
  const current = new Set(user.draftRoles ?? []);
  if (current.has(role)) {
    current.delete(role);
  } else {
    current.add(role);
  }

  if (current.size === 0) {
    current.add("ROLE_USER");
  }

  user.draftRoles = [...current];
}

function hasDraftRole(user, role) {
  return (user.draftRoles ?? []).includes(role);
}

function rolesChanged(user) {
  const a = [...(user.roles ?? [])].sort().join("|");
  const b = [...(user.draftRoles ?? [])].sort().join("|");
  return a !== b;
}

async function saveRoles(user) {
  savingUserId.value = user.id;
  error.value = "";
  success.value = "";

  try {
    const updated = await updateAdminUserRoles(user.id, user.draftRoles);

    const index = users.value.findIndex((u) => u.id === user.id);
    if (index >= 0) {
      users.value[index] = {
        ...updated,
        draftRoles: [...(updated.roles ?? [])],
      };
    }

    success.value = `Roles updated for ${updated.username}.`;
  } catch (e) {
    error.value = e.message || "Failed to update user roles.";
  } finally {
    savingUserId.value = null;
  }
}

onMounted(loadUsers);
</script>

<template>
  <main class="page">
    <section class="section">
      <div class="section-heading">
        <div>
          <h1>Admin users</h1>
          <p class="muted">
            Review accounts and update granted roles.
          </p>
        </div>

        <div class="toolbar">
          <input v-model="query" placeholder="Search users..."/>
          <button class="btn btn--ghost" @click="loadUsers">
            Refresh
          </button>
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>
      <p v-if="success" class="success">{{ success }}</p>

      <div v-if="loading" class="loader-block">
        <span class="loader-spinner"></span>
        <span class="muted">Loading users...</span>
      </div>

      <div v-else-if="filteredUsers.length" class="stack-list">
        <article v-for="user in filteredUsers" :key="user.id" class="card">
          <div class="toolbar toolbar--spread toolbar--top">
            <div>
              <h2>{{ user.displayName || user.username }}</h2>
              <p class="muted">
                #{{ user.id }} · {{ user.username }} · {{ user.email }}
              </p>
            </div>

            <span class="badge">
              {{ user.profilePublic ? "Public profile" : "Private profile" }}
            </span>
          </div>

          <div class="admin-role-group">
            <label
                v-for="role in editableRoles"
                :key="role"
                class="role-chip"
                :class="{ 'is-active': hasDraftRole(user, role) }"
            >
              <input
                  type="checkbox"
                  :checked="hasDraftRole(user, role)"
                  @change="toggleRole(user, role)"
              />
              <span>{{ role }}</span>
            </label>
          </div>

          <div class="toolbar">
            <button
                class="btn btn--primary"
                :disabled="savingUserId === user.id || !rolesChanged(user)"
                @click="saveRoles(user)"
            >
              {{ savingUserId === user.id ? "Saving..." : "Save roles" }}
            </button>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">
        <h3>No user found</h3>
        <p class="muted">Try another search term.</p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.stack-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar--spread {
  justify-content: space-between;
  align-items: center;
}

.toolbar--top {
  align-items: flex-start;
}

.badge {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 0.25rem 0.7rem;
  background: var(--surface-alt);
  font-size: 0.82rem;
}

.admin-role-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.role-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 0.45rem 0.85rem;
  background: #fff;
  cursor: pointer;
}

.role-chip.is-active {
  border-color: rgba(15, 118, 110, 0.35);
  background: var(--accent-green);
}

.role-chip input {
  margin: 0;
}
</style>