<script setup lang="ts">
type Variant = "primary";

withDefaults(defineProps<{
  variant?: Variant;
  type?: "button" | "submit" | "reset";
}>(), {
  variant: "primary",
  type: "button",
});
</script>

<template>
  <button :type="type" class="btn" :class="`btn--${variant}`">
    <slot />
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 45px;
  padding-inline: 20px;
  cursor: pointer;
  border: 0;
  &:hover {
    filter: brightness(0.95);
  }
}

.btn--primary {
}
</style>