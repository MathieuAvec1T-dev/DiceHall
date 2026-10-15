<script setup lang="ts">
import Header from "@/components/Header.vue";
import router from "@/router";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

(async () => {
  await userStore.ready;
  if (!userStore.user.id) {
    router.push("/login");
  }
})();
</script>

<template>
  <Header />
  <RouterView />
</template>