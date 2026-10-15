<script setup lang="ts">
</script>

<template>
  <div class="app">
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
