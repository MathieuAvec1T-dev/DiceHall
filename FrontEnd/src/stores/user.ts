import { ref } from "vue";
import { defineStore } from "pinia";

export const useUserStore = defineStore("user", () => {
  const token = ref(localStorage.getItem("token") ?? null);
  const userId = ref(localStorage.getItem("userId") ?? null);
  const user = ref({});

  if (token.value && userId.value) {
    fetch(`${window.location.protocol}//${window.location.hostname}:8080/users/auth/validate/${userId.value}`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${token.value}`,
      }
    })
    .then(res => res.json())
    .then(data => {
      delete data.token;
      user.value = data;
    });
  }
  

  function setUser(data: JSON) {
    user.value = data;
    if (data) {
      localStorage.setItem("userId", data.id);
    } else {
      localStorage.removeItem("userId");
    }
  }

  function setToken(tokenValue: string) {
    token.value = token
    if (tokenValue) {
      localStorage.setItem("token", tokenValue);
    } else {
      localStorage.removeItem("token");
    }
  }

  return {
    user,
    setUser,
    token,
    setToken
  };
});
