<script setup lang="ts">
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();

</script>

<template>
  <header class="header">
    <RouterLink to="/" class="header__logo">DiceHall</RouterLink>

    <nav class="header__nav" aria-label="Navigation principale">
      <template v-if="userStore.user.username">
        <RouterLink to="/campaigns">Campagnes</RouterLink>
        <RouterLink to="/characters">Personnages</RouterLink>
        <RouterLink to="/profile">Profil : {{ userStore.user.username }}</RouterLink>
      </template>
      <template v-else>
        <RouterLink to="/login">Connexion</RouterLink>
        <RouterLink to="/register">Inscription</RouterLink>
      </template>
    </nav>
  </header>
</template>

<style scoped>
.header {
  position: fixed;
  inset: 0 0 auto;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--header-height);
  padding-inline: 20px;
}

.header__logo {
  font-weight: 700;
  font-size: 1.25rem;
  text-decoration: none;
  color: inherit;
}

.header__nav {
  display: flex;
  gap: 24px;

  a {
    text-decoration: none;
    color: inherit;
  }

  a:hover,
  .router-link-active {
    text-decoration: underline;
  }
}
</style>
