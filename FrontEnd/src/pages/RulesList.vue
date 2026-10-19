<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { ref } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const userStore = useUserStore();

const error = ref(null);
const rules = ref([]);

fetch(`${window.location.protocol}//${window.location.hostname}:8080/campaigns/rules/${route.params.id}`, {
  method: "GET",
  headers : {
    "Authorization": `Bearer ${userStore.token}`
  }
}).then(async res => {
  const data = await res.json();
  if (res.ok) {
    rules.value = data;
  } else {
    error.value = data.message;
  }
});
</script>

<template>
    <span v-if="error">{{ error }}</span>
    <ul>
        <li v-for="rule in userStore.activeCampaign.value" v-bind:key="rule.id">
            <b>{{ rule.name }}</b>
            <p>{{ rule.description }}</p>
            <ul>
                <li v-for="tag in rule.tags" v-bind:key="tag.id">
                    {{ tag.name }}
                </li>
            </ul>
        </li>
    </ul>
</template>