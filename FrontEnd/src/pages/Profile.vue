<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { reactive, ref } from "vue";

const userStore = useUserStore();

const form = reactive({
  email: "",
  username: "",
  password: ""
});

(async () => {
  await userStore.ready;
  form.email = userStore.user.email;
  form.username = userStore.user.username;
  form.password = userStore.user.password;
})();

const errors = ref({});

async function handleSubmit() {
  errors.value = {};

  fetch(`${window.location.protocol}//${window.location.hostname}:8080/users/edit`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${userStore.token}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      email: form.email,
      username: form.username,
      password: form.password
    })
  }).then(async res => {
    const data = await res.json();
    if (res.ok) {
      userStore.user = data;
    } else {
      errors.value.global = data.message;
    }
  });
}

</script>

<template>
  <h1>Mon compte</h1>
  <form @submit.prevent="handleSubmit">
    <span v-if="errors.global">{{ errors.global }}</span>
    <div>
      <label for="email">Mail</label>
      <span v-if="errors.email" class="error">{{ errors.email }}</span>
      <input
        name="email"
        v-model.trim="form.email"
      />
    </div>
    <div>
      <label for="username">Pseudo</label>
      <span v-if="errors.username" class="error">{{ errors.username }}</span>
      <input
        name="username"
        v-model.trim="form.username"
      />
    </div>
    <div>
      <label for="password">Mot de passe</label>
      <span v-if="errors.password" class="error">{{ errors.password }}</span>
      <input
        name="password"
        v-model.trim="form.password"
        type="password"
      />
    </div>
    <input type="submit" value="Enregistrer"/>
  </form>
  <RouterLink to="/logout">Deconnexion</RouterLink>
</template>