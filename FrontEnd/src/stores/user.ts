import { ref } from "vue";
import { defineStore } from "pinia";

export const useUserStore = defineStore("user", () => {
  const token = ref(localStorage.getItem("token") ?? null);
  const user = ref({});

  if (token.value) {
    fetch(`${window.location.protocol}//${window.location.hostname}:8080/users/auth/validate`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${token.value}`,
      }
    }).then(res => {
      res.json().then(data => {
        if (res.ok) {
          delete data.token;
          user.value = data;
        }
      });  
    });
  }

  function setToken(tokenValue: string|null) {
    token.value = token;
    if (tokenValue) {
      localStorage.setItem("token", tokenValue);
    } else {
      localStorage.removeItem("token");
    }
  }

  return {
    user,
    token,
    setToken
  };
});
