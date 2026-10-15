<script setup lang="ts">
import router from "@/router";
import { useUserStore } from "@/stores/user";
import { ref, reactive } from "vue";

import SplitLayout from "@/components/layouts/SplitLayout.vue";
import ButtonLayout from "@/components/base/ButtonLayout.vue";

import authImage from "@/assets/images/auth.jpg";

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
    errors.value.email = "L'email est requis";
  }
  if (!form.username) {
    errors.value.username = "Le nom d'utilisateur est requis";
  }
  if (!form.password) {
    errors.value.password = "Le mot de passe est requis";
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
      }).then(async res => {
        const data = await res.json();
        if (res.ok) {
          userStore.setToken(data.token.value);
          delete data.token;
          userStore.user = data;
          router.push("/profile");
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
  <SplitLayout :image="authImage">
    <div class="auth">
      <div class="auth__intro">
        <h1>Inscrivez-vous</h1>
        <p>
          Créez votre compte pour gérer vos campagnes, vos personnages, lancer vos dés et
          suivre vos séances de jeu de rôle sur une seule application !
        </p>
      </div>

      <form class="auth__form" novalidate @submit.prevent="handleSubmit">
        <p v-if="errors.global" class="error">{{ errors.global }}</p>

        <div class="field">
          <label for="email">Adresse mail</label>
          <input
            id="email"
            name="email"
            type="email"
            autocomplete="email"
            v-model.trim="form.email"
          />
          <span v-if="errors.email" class="error">{{ errors.email }}</span>
        </div>

        <div class="field">
          <label for="username">Pseudo</label>
          <input
            id="username"
            name="username"
            autocomplete="username"
            v-model.trim="form.username"
          />
          <span v-if="errors.username" class="error">{{ errors.username }}</span>
        </div>

        <div class="field">
          <label for="password">Mot de passe</label>
          <input
            id="password"
            name="password"
            type="password"
            autocomplete="new-password"
            v-model="form.password"
          />
          <span v-if="errors.password" class="error">{{ errors.password }}</span>
        </div>

        <ButtonLayout type="submit" class="auth__submit">Inscription</ButtonLayout>
      </form>

      <p class="auth__divider">ou inscrivez-vous avec</p>

      <ul class="auth__socials" role="list">
        <li><button type="button">Google</button></li>
        <li><button type="button">Discord</button></li>
        <li><button type="button">Facebook</button></li>
      </ul>

      <div class="auth__footer">
        <p>
          Déjà un compte ? <RouterLink to="/login">Connectez-vous</RouterLink>
        </p>
      </div>
    </div>
  </SplitLayout>
</template>

<style scoped src="@/assets/styles/auth.css"></style>