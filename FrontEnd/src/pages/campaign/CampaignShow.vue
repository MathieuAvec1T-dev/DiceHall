<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { ref } from "vue";
import { RouterLink, useRoute } from "vue-router";

const error = ref(null);
const route = useRoute();
const userStore = useUserStore();
const campaign = ref({});
const myRole = ref();

fetch(`${window.location.protocol}//${window.location.hostname}:8080/campaigns/id/${route.params.id}`, {
  method: "GET",
  headers : {
    "Authorization": `Bearer ${userStore.token}`
  }
}).then(async res => {
  const data = await res.json();
  if (res.ok) {
    campaign.value = data;
    myRole.value = data.campaignUsers.find(user => user.id == userStore.user.id);
  } else {
    error.value = data.message;
  }
});

</script>

<template>
  <h1>{{ campaign.name }}</h1>
  <span v-if="error">{{ error }}</span>
  <RouterLink :to="`/campaign/${campaign.id}/rules`">Règles</RouterLink>
  <h3>Listes des joueurs :</h3>
  <ul>
    <li v-for="player in campaign.campaignUsers" v-bind:key="player.id">
      {{ player.username }} - {{ player.role }}
      <div v-if="myRole && myRole.role == 'Owner' && player.id != userStore.user.id">
        <button v-if="player.role != 'Admin'">Nommer administrateur</button>
        <button v-else>Retirer administrateur</button>
      </div>
    </li>
  </ul>
  <RouterLink :to="`/campaign/${campaign.id}/join`" v-if="!myRole">Rejoindre la campagne</RouterLink>
  <RouterLink to="/rules">Voir les règles de la campagne</RouterLink>
</template>