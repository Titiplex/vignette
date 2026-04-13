<script setup>
import {RouterLink} from "vue-router";

const year = new Date().getFullYear();

const productLinks = [
  {label: "Home", to: "/"},
  {label: "Languages", to: "/languages"},
  {label: "Scenarios", to: "/scenarios"},
  {label: "About the project", to: "/about"},
];

const resourceLinks = [
  {label: "GitHub repository", href: "https://github.com/Titiplex/vignette"},
  {label: "API documentation", href: "https://titiplex.github.io/vignette/"},
];
</script>

<template>
  <footer class="app-footer">
    <div class="app-container app-footer__inner app-footer__grid">
      <section class="app-footer__brand">
        <h2 class="app-footer__title">Vignette</h2>
        <p class="app-footer__text">
          A platform for building multimedia scenarios, collecting oral data,
          and supporting richer language documentation workflows.
        </p>
        <p class="app-footer__meta">© {{ year }} Vignette</p>
      </section>

      <section>
        <h3 class="app-footer__heading">Navigate</h3>
        <ul class="app-footer__list">
          <li v-for="link in productLinks" :key="link.to">
            <RouterLink :to="link.to">{{ link.label }}</RouterLink>
          </li>
        </ul>
      </section>

      <section>
        <h3 class="app-footer__heading">Resources</h3>
        <ul class="app-footer__list">
          <li v-for="link in resourceLinks" :key="link.href">
            <a :href="link.href" target="_blank" rel="noreferrer">
              {{ link.label }}
            </a>
          </li>
        </ul>
      </section>
    </div>
  </footer>
</template>