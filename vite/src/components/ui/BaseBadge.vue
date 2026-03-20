<script setup>
defineProps({
  variant: {
    type: String,
    default: "neutral",
    validator: (v) =>
        ["neutral", "info", "success", "warning", "danger"].includes(v),
  },
});
</script>

<template>
  <span class="badge" :class="`badge--${variant}`">
    <slot/>
  </span>
</template>