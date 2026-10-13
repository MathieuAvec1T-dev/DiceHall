import CampaignNew from "@/pages/campaign/CampaignNew.vue";
import CampaignsList from "@/pages/campaign/CampaignsList.vue";
import Homepage from "@/pages/Homepage.vue";
import Login from "@/pages/Login.vue";
import Logout from "@/pages/Logout.vue";
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
  }, {
    path: "/logout",
    name: "logout",
    component: Logout
  }, {
    path: "/campaigns",
    name: "campaignsList",
    component : CampaignsList
  }, {
    path: "/campaign/new",
    name: "campaignNew",
    component: CampaignNew
  }]
});

export default router;
