<script setup lang="ts">
import Header from "@/components/Header.vue";
</script>

<template>
  <Header />
  <RouterView />
</template>

<style scoped>
header {
  background-color: grey;
  display: flex;
}
header > * {
  margin: auto;
}
</style>
