<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { ref } from "vue";

const userStore = useUserStore();
const campaigns = ref([]);

(async () => {
  await userStore.ready;
  if (userStore.user.id) {
    fetch(`${window.location.protocol}//${window.location.hostname}:8080/campaigns/user/${userStore.user.id}?addPrivate=true`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${userStore.token}`,
      }
    }).then(async res => {
      const data = await res.json();
      if (res.ok) {
        campaigns.value = data;
      }
    });
  }
})();

function activeCampaign(e) {
  fetch(`${window.location.protocol}//${window.location.hostname}:8080/campaigns/active/${e.target.options[e.target.selectedIndex].value}`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${userStore.token}`,
    }
  }).then(async res => {
    const data = await res.json();
    if (res.ok) {
      userStore.activeCampaign = data;
    }
  });
}

</script>

<template>
  <header class="header">
    <RouterLink to="/" class="header__logo">DiceHall</RouterLink>

    <nav class="header__nav" aria-label="Navigation principale">
      <template v-if="userStore.user.username">
        <RouterLink to="/campaigns">Campagnes</RouterLink>
        <RouterLink to="/characters">Personnages</RouterLink>
        <div v-if="campaigns.length > 0">
          Campagne active :
          <select v-on:change="activeCampaign">
            <option v-for="campaign in campaigns" v-bind:key="campaign.id" :value="campaign.id" :selected="campaign.id == userStore.activeCampaign.id">
              {{ campaign.name }}
            </option>
          </select>
        </div>
        <div v-else>
          Pas de campagne active
        </div>
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
