<script setup>
defineProps({
  type: {
    type: String,
    default: "info",
    validator: (v) => ["info", "success", "error"].includes(v),
  },
});
</script>

<template>
  <div class="alert" :class="`alert--${type}`">
    <slot/>
  </div>
</template>