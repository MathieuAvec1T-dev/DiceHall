<script setup lang="ts">
import router from "@/router";
import { useUserStore } from '@/stores/user';
import { useRoute } from 'vue-router';

const route = useRoute();
const userStore = useUserStore();

fetch(`${window.location.protocol}//${window.location.hostname}:8080/campaigns/join/${route.params.id}`, {
  method: "POST",
  headers: {
  "Authorization": `Bearer ${userStore.token}`,
  }
}).then(async res => {
  const data = await res.json();
  if (!userStore.activeCampaign.id) {
    userStore.activeCampaign = data;
    window.location.href = `/campaign/${route.params.id}`;
  } else {
    router.push(`/campaign/${route.params.id}`);
  }
});

</script>
