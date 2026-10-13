<script setup lang="ts">
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

</script>

<template>
  <header>
    <div>DICE HALL</div>
    <nav v-if="userStore.user.username">
      <RouterLink to="/campaigns">Campagnes</RouterLink>
      <RouterLink to="/characters">Personnages</RouterLink>
      <RouterLink to="/profile">Profil : {{ userStore.user.username }}</RouterLink>
    </nav>
    <nav v-else>
      <RouterLink to="/register">Inscription</RouterLink>
      <RouterLink to="/login">Connexion</RouterLink>
    </nav>
  </header>
</template>