<script setup lang="ts">
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

</script>

<template>
  <header>
    <div>DICE HALL</div>
    <nav v-if="userStore.token">
      <RouterLink to="/campaign">Campagnes</RouterLink>
      <RouterLink to="/character">Personnages</RouterLink>
      <RouterLink to="/me">Profil : {{ userStore.user.username }}</RouterLink>
    </nav>
    <nav v-else>
      <RouterLink to="/register">Inscription</RouterLink>
      <RouterLink to="/login">Connexion</RouterLink>
    </nav>
  </header>
</template>