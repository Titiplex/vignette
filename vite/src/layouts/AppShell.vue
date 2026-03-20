<template>
  <div class="app-shell">
    <AppHeader/>
    <BaseToast/>

    <div class="app-shell__body">
      <slot/>
    </div>

    <footer class="app-footer">
      <div class="app-container app-footer__inner">
        <div>
          <strong>Vignette</strong>
          <p class="app-footer__text">
            Community-friendly multimedia scenarios for language documentation.
          </p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import AppHeader from "../components/AppHeader.vue";
import BaseToast from "../components/ui/BaseToast.vue";
</script>