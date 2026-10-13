<script setup lang="ts">
import router from "@/router";
import { useUserStore } from "@/stores/user";
import { ref, reactive } from "vue";

const userStore = useUserStore();

const form = reactive({
  name: "",
  private: false
});

const errors = ref({});

async function handleSubmit() {
  errors.value = {};

  if (!form.name) {
    errors.value.name = "Le nom est requis";
  }

  if (Object.keys(errors.value).length === 0) {
    try {
      await fetch(`${window.location.protocol}//${window.location.hostname}:8080/campaigns`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${userStore.token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          name: form.name,
          privacy: form.private
        })
      }).then(async res => {
        const data = await res.json();
        if (res.ok) {
          router.push("/campaigns");
        } else {
          errors.value.global = data.message;
        }
      });
    } catch (error) {
      errors.value.global = "Une erreur est survenu";
    }
  }
}

</script>

<template>
    <h1>Créer une campagne</h1>
    <form @submit.prevent="handleSubmit">
      <span v-if="errors.global">{{ errors.global }}</span>
      <div>
        <label for="name">Nom de la campagne</label>
        <span v-if="errors.name" class="error">{{ errors.name }}</span>
        <input
          name="name"
          v-model.trim="form.name"
        />
      </div>
      <div hidden>
        <label for="private">Private ?</label>
        <span v-if="errors.private" class="error">{{ errors.private }}</span>
        <input
          name="private"
          v-model.trim="form.private"
          type="checkbox"
        />
      </div>
      <input type="submit" value="Créer la campagne"/>
    </form>
</template>