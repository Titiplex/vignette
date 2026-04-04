<script setup>
import {onMounted, ref} from "vue";
import {
  fetchAccreditationRequests,
  fetchAccreditations,
  grantAccreditation,
  reviewAccreditationRequest,
} from "../api/community";

const loading = ref(false);
const error = ref("");
const success = ref("");

const permissionType = ref("LANGUAGE_EDIT");
const requests = ref([]);
const accreditations = ref([]);

const grantForm = ref({
  userId: "",
  permissionType: "LANGUAGE_EDIT",
  note: "",
  targetId: "",
});

async function loadAll() {
  loading.value = true;
  error.value = "";
  success.value = "";

  try {
    requests.value = await fetchAccreditationRequests(
        permissionType.value,
        "GLOBAL",
        ""
    );

    accreditations.value = await fetchAccreditations(
        permissionType.value,
        "GLOBAL",
        ""
    );
  } catch (e) {
    error.value = e.message || "Failed to load admin community data.";
  } finally {
    loading.value = false;
  }
}

async function reviewRequest(requestId, approved) {
  error.value = "";
  success.value = "";

  try {
    await reviewAccreditationRequest(requestId, {
      approved,
      reviewNote: approved ? "Approved by admin." : "Rejected by admin.",
    });
    success.value = approved ? "Request approved." : "Request rejected.";
    await loadAll();
  } catch (e) {
    error.value = e.message || "Failed to review request.";
  }
}

async function submitGrant() {
  error.value = "";
  success.value = "";

  try {
    await grantAccreditation({
      userId: Number(grantForm.value.userId),
      permissionType: grantForm.value.permissionType,
      scopeType: "GLOBAL",
      targetId: null,
      note: grantForm.value.note,
    });

    success.value = "Accreditation granted.";
    grantForm.value.userId = "";
    grantForm.value.note = "";
    await loadAll();
  } catch (e) {
    error.value = e.message || "Failed to grant accreditation.";
  }
}

onMounted(loadAll);
</script>

<template>
  <main class="page">
    <section class="section">
      <div class="section-heading">
        <div>
          <h1>Admin community</h1>
          <p class="muted">
            Global accreditation moderation and direct grants.
          </p>
        </div>

        <label>
          Permission filter
          <select v-model="permissionType" @change="loadAll">
            <option value="COMMUNITY_REVIEW">COMMUNITY_REVIEW</option>
            <option value="LANGUAGE_EDIT">LANGUAGE_EDIT</option>
            <option value="SCENARIO_EDIT">SCENARIO_EDIT</option>
            <option value="SCENARIO_MODERATE">SCENARIO_MODERATE</option>
          </select>
        </label>
      </div>

      <p v-if="error" class="error">{{ error }}</p>
      <p v-if="success" class="success">{{ success }}</p>

      <div v-if="loading" class="loader-block">
        <span class="loader-spinner"></span>
        <span class="muted">Loading admin community data...</span>
      </div>

      <template v-else>
        <div class="admin-grid">
          <section class="card">
            <h2>Accreditation requests</h2>

            <div v-if="requests.length" class="stack-list">
              <article v-for="req in requests" :key="req.id" class="card card--nested">
                <p><strong>#{{ req.id }}</strong> · {{ req.requesterUsername }}</p>
                <p class="muted">
                  {{ req.permissionType }} · {{ req.scopeType }} · {{ req.status }}
                </p>
                <p class="text">{{ req.motivation || "No motivation provided." }}</p>

                <div v-if="req.status === 'PENDING'" class="toolbar">
                  <button class="btn btn--primary" @click="reviewRequest(req.id, true)">Approve</button>
                  <button class="btn btn--ghost" @click="reviewRequest(req.id, false)">Reject</button>
                </div>
              </article>
            </div>

            <p v-else class="muted">No request found.</p>
          </section>

          <section class="card">
            <h2>Granted accreditations</h2>

            <div v-if="accreditations.length" class="stack-list">
              <article v-for="acc in accreditations" :key="acc.id" class="card card--nested">
                <p><strong>#{{ acc.id }}</strong> · {{ acc.username }}</p>
                <p class="muted">
                  {{ acc.permissionType }} · {{ acc.scopeType }}
                </p>
                <p class="muted">{{ acc.note || "No note." }}</p>
              </article>
            </div>

            <p v-else class="muted">No accreditation found.</p>

            <hr class="separator"/>

            <h3>Grant manually</h3>

            <div class="form-grid">
              <label>
                User ID
                <input v-model="grantForm.userId" type="number" min="1"/>
              </label>

              <label>
                Permission
                <select v-model="grantForm.permissionType">
                  <option value="COMMUNITY_REVIEW">COMMUNITY_REVIEW</option>
                  <option value="LANGUAGE_EDIT">LANGUAGE_EDIT</option>
                  <option value="SCENARIO_EDIT">SCENARIO_EDIT</option>
                  <option value="SCENARIO_MODERATE">SCENARIO_MODERATE</option>
                </select>
              </label>

              <label class="field--full">
                Note
                <textarea v-model="grantForm.note" rows="3"/>
              </label>
            </div>

            <div class="toolbar">
              <button class="btn btn--primary" @click="submitGrant">
                Grant
              </button>
            </div>
          </section>
        </div>
      </template>
    </section>
  </main>
</template>

<style scoped>
.admin-grid,
.stack-list {
  display: grid;
  gap: 16px;
}

.admin-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.card--nested {
  border-radius: 14px;
  background: var(--surface-alt);
}

.separator {
  border: none;
  border-top: 1px solid var(--border);
  margin: 12px 0;
}

.field--full {
  grid-column: 1 / -1;
}

@media (max-width: 900px) {
  .admin-grid {
    grid-template-columns: 1fr;
  }
}
</style>