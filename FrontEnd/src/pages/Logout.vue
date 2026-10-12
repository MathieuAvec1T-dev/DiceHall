<script setup lang="ts">
import router from "@/router";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

fetch(`${window.location.protocol}//${window.location.hostname}:8080/users/auth/disconnect`, {
  method: "POST",
  headers: {
  "Authorization": `Bearer ${userStore.token}`,
  }
}).then(() => {
  userStore.user = {};
  userStore.setToken(null);
  router.push("/");
});

</script>

<template>
  <div></div>
</template>