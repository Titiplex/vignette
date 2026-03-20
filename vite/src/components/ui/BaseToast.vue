<script setup>
import {useToast} from "../../composables/useToast";

const {toasts, remove} = useToast();
</script>

<template>
  <div class="toast-stack">
    <div
        v-for="toast in toasts"
        :key="toast.id"
        class="toast"
        :class="`toast--${toast.type}`"
    >
      <span>{{ toast.message }}</span>
      <button class="toast__close" type="button" @click="remove(toast.id)">
        ×
      </button>
    </div>
  </div>
</template>