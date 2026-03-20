<script setup>
defineProps({
  title: {type: String, required: true},
  subtitle: {type: String, default: ""},
});
</script>

<template>
  <div class="section-heading">
    <div>
      <h1>{{ title }}</h1>
      <p v-if="subtitle" class="muted">{{ subtitle }}</p>
    </div>

    <div class="page-header__actions">
      <slot name="actions"/>
    </div>
  </div>
</template>