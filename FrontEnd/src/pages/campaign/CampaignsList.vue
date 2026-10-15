<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { ref } from "vue";

const userStore = useUserStore();

const error = ref(null);
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
      } else {
        error.value = data.message;
      }
    });
  }
})();
</script>

<script lang="ts">
</script>

<template>
  <h1>Liste de mes campagnes</h1>
  <span v-if="error">{{ error }}</span>
  <ul>
    <li v-for="campaign in campaigns" v-bind:key="campaign.id">
      <RouterLink :to="'/campaign/' + campaign.id">
        <div>
          {{ campaign.name }} {{ campaign.campaignUsers.find(user => user.id == userStore.user.id).role }}
        </div>
        <div>
          {{ "Nom du/des personnages du joueur" }}
        </div>
        <div>
          {{ campaign.campaignUsers.length }} Joueurs
        </div>
      </RouterLink>
    </li>
    <div v-if="0 == campaigns.length">
      Aucune campagne
    </div>
  </ul>
  <div>
    <RouterLink to="/campaign/new">Créer une campagne</RouterLink>
  </div>
</template>