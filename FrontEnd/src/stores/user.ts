import { ref } from "vue";
import { defineStore } from "pinia";

export const useUserStore = defineStore("user", () => {
  const token = ref(localStorage.getItem("token") ?? null);
  const user = ref({});
  const activeCampaign = ref({});

  let ready = Promise.resolve();

  if (token.value) {
    ready = fetch(`${window.location.protocol}//${window.location.hostname}:8080/users/auth/validate`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${token.value}`,
      }
    }).then(async res => {
      const data = await res.json();
      if (res.ok) {
        delete data.token;
        activeCampaign.value = data.activeCampaign;
        delete data.activeCampaign;
        user.value = data;
      } 
    });
  }

  function setToken(tokenValue: string|null) {
    token.value = tokenValue;
    if (tokenValue) {
      localStorage.setItem("token", tokenValue);
    } else {
      localStorage.removeItem("token");
    }
  }

  return {
    ready,
    user,
    activeCampaign,
    token,
    setToken
  };
});
