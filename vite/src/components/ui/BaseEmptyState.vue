<script setup>
defineProps({
  title: {type: String, required: true},
  message: {type: String, default: ""},
});
</script>

<template>
  <div class="card empty-state">
    <h3>{{ title }}</h3>
    <p v-if="message" class="muted">{{ message }}</p>
    <slot/>
  </div>
</template>