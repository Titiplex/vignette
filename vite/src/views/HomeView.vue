<script setup>
import {RouterLink} from "vue-router";
</script>

<template>
  <main class="page">
    <section class="hero hero--enhanced">
      <div class="hero__content">
        <span class="eyebrow">Research and community platform</span>
        <h1>Build richer language documentation workflows from scenes, audio and collaborative analysis</h1>
        <p class="hero__text">
          Vignette helps create multimedia elicitation scenarios for language documentation,
          especially when simple translation prompts are not enough. It combines visual prompts,
          oral recordings, discussions and collaborative curation in one place.
        </p>

        <div class="hero__actions">
          <RouterLink to="/scenarios" class="btn btn--primary">
            Browse scenarios
          </RouterLink>
          <RouterLink to="/languages" class="btn btn--ghost">
            Explore languages
          </RouterLink>
          <RouterLink to="/about" class="btn btn--ghost">
            Learn about the project
          </RouterLink>
        </div>
      </div>

      <div class="hero-stats">
        <article class="card">
          <h3>Why it exists</h3>
          <p class="text">
            Many documentation workflows rely too heavily on translation. Vignette pushes toward
            scene-based, contextual, multimodal elicitation.
          </p>
        </article>

        <article class="card">
          <h3>What it supports</h3>
          <p class="text">
            Scenarios, thumbnails, marker-linked audio, language metadata and community discussion.
          </p>
        </article>

        <article class="card">
          <h3>Who it is for</h3>
          <p class="text">
            Researchers, students, documentation teams, and language communities working on low-resource languages.
          </p>
        </article>
      </div>
    </section>

    <section class="section">
      <div class="section-heading">
        <div>
          <h2>How it works</h2>
          <p class="muted">
            A workflow built for documentation, elicitation, and collaboration.
          </p>
        </div>
      </div>

      <div class="card-grid">
        <article class="card">
          <h3>1. Explore language entries</h3>
          <p class="text">
            Start from existing language entries, metadata, genealogy and linked scenarios.
          </p>
        </article>

        <article class="card">
          <h3>2. Build scenario sequences</h3>
          <p class="text">
            Use image-driven prompts to create richer elicitation contexts than plain questionnaire-style prompts.
          </p>
        </article>

        <article class="card">
          <h3>3. Attach oral responses</h3>
          <p class="text">
            Record and organize audio directly against thumbnails and markers to preserve situated speech.
          </p>
        </article>

        <article class="card">
          <h3>4. Discuss and refine</h3>
          <p class="text">
            Use discussion threads for notes, glossing, translation, interpretation and collaborative review.
          </p>
        </article>

        <article class="card">
          <h3>5. Document language use</h3>
          <p class="text">
            Build reusable, inspectable material for low-resource and endangered language work.
          </p>
        </article>

        <article class="card">
          <h3>6. Publish progressively</h3>
          <p class="text">
            Let projects grow from drafts to shareable resources without forcing a rigid all-at-once workflow.
          </p>
        </article>
      </div>
    </section>

    <section class="section">
      <div class="section-heading">
        <div>
          <h2>Useful links</h2>
          <p class="muted">Core project and technical resources.</p>
        </div>
      </div>

      <div class="card-grid">
        <article class="card">
          <h3>Repository</h3>
          <p class="text">Source code and development history.</p>
          <a href="https://github.com/Titiplex/vignette" target="_blank" rel="noreferrer">
            Open GitHub repository
          </a>
        </article>

        <article class="card">
          <h3>API documentation</h3>
          <p class="text">Browse endpoints, models and permissions.</p>
          <a href="https://titiplex.github.io/vignette/" target="_blank" rel="noreferrer">
            Open API documentation
          </a>
        </article>

        <article class="card">
          <h3>Project overview</h3>
          <p class="text">Read the context, goals and design choices.</p>
          <RouterLink to="/about">
            Open project page
          </RouterLink>
        </article>
      </div>
    </section>
  </main>
</template>