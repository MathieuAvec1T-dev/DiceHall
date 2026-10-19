import CampaignNew from "@/pages/campaign/CampaignNew.vue";
import CampaignsList from "@/pages/campaign/CampaignsList.vue";
import Login from "@/pages/auth/Login.vue";
import Logout from "@/pages/auth/Logout.vue";
import Register from "@/pages/auth/Register.vue";
import { createRouter, createWebHistory } from "vue-router";
import Auth from "@/layers/Auth.vue";
import Guest from "@/layers/Guest.vue";
import Profile from "@/pages/Profile.vue";
import CampaignShow from "@/pages/campaign/CampaignShow.vue";
import CampaignJoin from "@/pages/campaign/CampaignJoin.vue";
import RulesList from "@/pages/RulesList.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [{
    path: "/",
    name: "guest",
    component: Guest,
    children: [{
      path: "/register",
      name: "register",
      component: Register
    }, {
      path: "/login",
      name: "login",
      component: Login
    }]
  }, {
    name: "auth",
    path: "/",
    component: Auth,
    children: [{
      path: "/logout",
      name: "logout",
      component: Logout
    }, {
      path: "/profile",
      name: "profile",
      component: Profile
    }, {
      path: "/campaigns",
      name: "campaignsList",
      component: CampaignsList
    }, {
      path: "/campaign/new",
      name: "campaignNew",
      component: CampaignNew
    }, {
      path: "/campaign/:id",
      name: "campaignShow",
      component: CampaignShow
    }, {
      path: "/campaign/:id/join",
      name: "campaignJoin",
      component: CampaignJoin
    }, {
      path: "/campaign/:id/rules",
      name: "rulesList",
      component: RulesList
    }]
  }]
});

export default router;
