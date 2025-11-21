<template>
  <div class="flex items-center gap-3">
    <button
      type="button"
      @click="emit('login')"
      class="bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white px-4 py-2 rounded-lg font-medium transition-all"
    >
      로그인
    </button>
  </div>
</template>

<script setup>
const emit = defineEmits(['login'])
</script>
