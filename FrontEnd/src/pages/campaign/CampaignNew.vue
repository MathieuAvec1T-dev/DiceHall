<script setup lang="ts">
import router from "@/router";
import { useUserStore } from "@/stores/user";
import { ref, reactive } from "vue";

const userStore = useUserStore();

const form = reactive({
  name: "",
  private: false,
  rules: []
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
          privacy: form.private,
          ruleIDs: form.rules
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

// @TODO : mock
const rules = [{
  id: 1,
  name: 'test1',
  description: 'desc1'
}, {
  id: 2,
  name: 'test2',
  description: 'desc2'
}];

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
      <div>
        <label for="rules">Règles</label>
        <span v-if="errors.rules" class="error">{{ errors.rules }}</span>
        <div>
          <div v-for="rule in rules" v-bind:key="rule.id">
            <input
              name="rules[]"
              v-model.trim="form.rules"
              type="checkbox"
              :value="rule.id"
            />
            <span>{{ rule.name }}</span>
          </div>
        </div>
      </div>
      <input type="submit" value="Créer la campagne"/>
    </form>
</template>