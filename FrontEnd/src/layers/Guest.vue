<script setup lang="ts">
import router from "@/router";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

(async () => {
  await userStore.ready;
  if (userStore.user.id) {
    router.push("/profile");
  }
})();
</script>

<template>
  <RouterView />
</template>