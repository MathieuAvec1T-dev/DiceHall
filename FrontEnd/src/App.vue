<script setup lang="ts">
import Header from "@/components/Header.vue";
</script>

<template>
  <div class="app">
    <Header />
    <main>
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app {
  display: flex;
  flex-direction: column;
  min-height: 100dvh;
}

main {
  display: grid;
  flex: 1;
}
</style>
