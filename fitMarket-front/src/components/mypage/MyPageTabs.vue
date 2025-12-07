<template>
  <div class="flex flex-wrap gap-3">
    <button
      v-for="tab in tabs"
      :key="tab.value"
      class="flex items-center gap-2 px-5 py-3 rounded-xl font-semibold transition-colors"
      :class="activeTab === tab.value ? 'bg-green-600 text-white shadow-lg' : 'bg-white text-gray-600 border border-gray-200 hover:border-green-200'"
      @click="handleChange(tab.value)"
    >
      <component :is="tab.icon" class="w-4 h-4" />
      {{ tab.label }}
    </button>
  </div>
</template>

<script setup>
const props = defineProps({
  tabs: {
    type: Array,
    required: true,
  },
  activeTab: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(['change']);

const handleChange = (value) => {
  emit('change', value);
};
</script>
