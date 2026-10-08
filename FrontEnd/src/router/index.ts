import Homepage from "@/pages/Homepage.vue";
import Login from "@/pages/Login.vue";
import Register from "@/pages/Register.vue";
import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [{
    path: "/",
    name: "homepage",
    component: Homepage
  }, {
    path: "/register",
    name: "register",
    component: Register
  }, {
    path: "/login",
    name: "login",
    component: Login
  }],
});

export default router;
