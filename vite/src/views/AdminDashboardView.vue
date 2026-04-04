<script setup>
import {RouterLink} from "vue-router";
</script>

<template>
  <main class="page">
    <section class="section">
      <div class="section-heading">
        <div>
          <h1>Admin dashboard</h1>
          <p class="muted">
            Entry point for current global moderation tools.
          </p>
        </div>
      </div>

      <div class="card-grid">
        <section class="card">
          <h2>Community administration</h2>
          <p class="text">
            Moderate global accreditation requests and grant accreditations directly.
          </p>
          <RouterLink to="/admin/community" class="btn btn--primary">
            Open community admin
          </RouterLink>
        </section>

        <section class="card">
          <h2>Languages administration</h2>
          <p class="text">
            Search languages, inspect entries and open edit-oriented admin workflows.
          </p>
          <RouterLink to="/admin/languages" class="btn btn--primary">
            Open language admin
          </RouterLink>
        </section>

        <section class="card">
          <h2>Current limitation</h2>
          <p class="text">
            This dashboard is front-ready, but true global user/scenario back-office still needs dedicated admin
            endpoints in the backend.
          </p>
        </section>
      </div>
    </section>
  </main>
</template>