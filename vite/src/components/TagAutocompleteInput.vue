<script setup>
import {computed, ref, watch} from "vue";
import {fetchScenarioTagSuggestions} from "../api/scenarioTags";

const props = defineProps({
  modelValue: {type: Array, default: () => []},
  label: {type: String, default: "Tags"},
  placeholder: {type: String, default: "Type a tag and press Enter"},
});

const emit = defineEmits(["update:modelValue"]);

const input = ref("");
const suggestions = ref([]);
const loading = ref(false);

const selected = computed(() => Array.isArray(props.modelValue) ? props.modelValue : []);

function normalizeDisplay(value) {
  return String(value ?? "").trim().replaceAll(/\s+/g, " ");
}

function addTag(value) {
  const cleaned = normalizeDisplay(value);
  if (!cleaned) return;

  const exists = selected.value.some(
      tag => tag.toLowerCase() === cleaned.toLowerCase()
  );
  if (exists) {
    input.value = "";
    return;
  }

  emit("update:modelValue", [...selected.value, cleaned]);
  input.value = "";
  suggestions.value = [];
}

function removeTag(tagToRemove) {
  emit("update:modelValue", selected.value.filter(tag => tag !== tagToRemove));
}

async function loadSuggestions() {
  loading.value = true;
  try {
    const data = await fetchScenarioTagSuggestions(input.value.trim(), 8);
    const names = Array.isArray(data) ? data.map(item => item.name) : [];
    suggestions.value = names.filter(
        name => !selected.value.some(tag => tag.toLowerCase() === name.toLowerCase())
    );
  } catch {
    suggestions.value = [];
  } finally {
    loading.value = false;
  }
}

function onKeydown(event) {
  if (event.key === "Enter" || event.key === ",") {
    event.preventDefault();
    addTag(input.value);
  }
  if (event.key === "Backspace" && !input.value && selected.value.length) {
    removeTag(selected.value[selected.value.length - 1]);
  }
}

watch(input, (value) => {
  if (!value.trim()) {
    suggestions.value = [];
    return;
  }
  loadSuggestions();
});
</script>

<template>
  <label class="tag-input">
    {{ label }}

    <div class="tag-input__box">
      <span
          v-for="tag in selected"
          :key="tag"
          class="tag-chip"
      >
        {{ tag }}
        <button type="button" class="tag-chip__remove" @click="removeTag(tag)">×</button>
      </span>

      <input
          v-model="input"
          :placeholder="placeholder"
          @keydown="onKeydown"
      />
    </div>

    <div v-if="input.trim()" class="tag-input__dropdown">
      <button
          v-for="suggestion in suggestions"
          :key="suggestion"
          type="button"
          class="tag-input__option"
          @click="addTag(suggestion)"
      >
        {{ suggestion }}
      </button>

      <button
          v-if="!suggestions.some(s => s.toLowerCase() === input.trim().toLowerCase())"
          type="button"
          class="tag-input__option tag-input__option--create"
          @click="addTag(input)"
      >
        Create tag “{{ input.trim() }}”
      </button>

      <div v-if="loading" class="muted">Loading suggestions...</div>
    </div>
  </label>
</template>

<style scoped>
.tag-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tag-input__box {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--surface);
}

.tag-input__box input {
  border: none;
  outline: none;
  flex: 1;
  min-width: 180px;
  background: transparent;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: var(--accent-cool);
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 6px 10px;
}

.tag-chip__remove {
  border: none;
  background: transparent;
  cursor: pointer;
}

.tag-input__dropdown {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--surface-alt);
}

.tag-input__option {
  text-align: left;
  border: none;
  background: transparent;
  padding: 8px 10px;
  border-radius: 10px;
  cursor: pointer;
}

.tag-input__option:hover {
  background: var(--accent-cool);
}

.tag-input__option--create {
  font-weight: 600;
}
</style>