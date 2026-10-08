<script setup lang="ts">
import router from "@/router";
import { useUserStore } from "@/stores/user";
import { ref, reactive } from "vue";

const userStore = useUserStore();

const form = reactive({
  email: "",
  username: "",
  password: ""
});

const errors = ref({});

async function handleSubmit() {
  errors.value = {};

  if (!form.email) {
    errors.value.email = "L'email est requis"
  }
  if (!form.username) {
    errors.value.username = "Le nom d'utilisateur est requis"
  }
  if (!form.password) {
    errors.value.password = "Le mot de passe est requis"
  }

  if (Object.keys(errors.value).length === 0) {
    try {
      await fetch(`${window.location.protocol}//${window.location.hostname}:8080/users`, {
        method: "POST",
        headers: {
          "Content-Type": "Application/Json"
        },
        body: JSON.stringify({
          email: form.email,
          username: form.username,
          password: form.password
        })
      })
      .then(res => res.json())
      .then(data => {
        userStore.setToken(data.token.value);
        delete data.token;
        userStore.setUser(data);
        router.push("/");
      });
    } catch (error) {
      errors.value.global = "Une erreur est survenu";
    }
  }
}

</script>

<template>
  <div>
    <h1>Inscription</h1>
    <form @submit.prevent="handleSubmit">
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
      <input type="submit" value="S'inscrire"/>
    </form>
    <RouterLink to="login">J'ai déjà un compte</RouterLink>
  </div>
</template>