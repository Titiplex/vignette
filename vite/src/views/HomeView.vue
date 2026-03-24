<script setup>
import {RouterLink} from "vue-router";
</script>

<template>
  <main class="page">
    <section class="hero">
      <div class="hero__content">
        <span class="eyebrow">Research platform</span>
        <h1>Build multimedia elicitation scenarios for endangered languages</h1>
        <p class="hero__text">
          Vignette helps researchers and communities create image-based scenarios,
          collect oral responses, and document language use in richer, less translation-driven contexts.
        </p>

        <div class="hero__actions">
          <RouterLink to="/scenarios" class="btn btn--primary">
            Browse scenarios
          </RouterLink>
          <RouterLink to="/languages" class="btn btn--ghost">
            Explore languages
          </RouterLink>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section-heading">
        <div>
          <h2>How it works</h2>
          <p class="muted">
            A workflow built for documentation, elicitation, and community-friendly collection.
          </p>
        </div>
      </div>

      <div class="card-grid">
        <article class="card">
          <h3>Explore language data</h3>
          <p class="text">
            Browse language entries and related scenario usage to ground your work in an existing catalog.
          </p>
        </article>

        <article class="card">
          <h3>Create scenario sequences</h3>
          <p class="text">
            Build vignette-based prompts using images that support narrative and situated elicitation.
          </p>
        </article>

        <article class="card">
          <h3>Record oral responses</h3>
          <p class="text">
            Attach audio directly to visual prompts to preserve pronunciation, discourse, and local interpretation.
          </p>
        </article>

        <article class="card">
          <h3>Reduce translation bias</h3>
          <p class="text">
            Work from scenes and local meaning rather than relying only on direct translation pipelines.
          </p>
        </article>

        <article class="card">
          <h3>Document language use</h3>
          <p class="text">
            Participate to the effort to document low-resource language use in a more accessible way.
          </p>
        </article>
      </div>
      <div>
        <article class="card">
          <h3>Links</h3>
          <ul>
            <li><a>Github Repository</a></li>
            <li><a>Project Report</a></li>
            <li><a>API Documentation</a></li>
          </ul>
        </article>
      </div>
    </section>
  </main>
</template>