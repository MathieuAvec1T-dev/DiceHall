<script setup lang="ts">
withDefaults(defineProps<{
	image: string;
	imageAlt?: string; // Leave empty if the image is decorative: alt="" makes screen readers skip it.
}>(), {
	imageAlt: "",
});
</script>

<template>
	<div class="split">
		<div class="split__media">
			<!-- Keep the alt attribute even when empty: without it, screen readers read the file name aloud. -->
			<img :src="image" :alt="imageAlt" />
		</div>
		<div class="split__content">
			<slot />
		</div>
	</div>
</template>

<style scoped>
.split {
	display: grid;
	grid-template-columns: 1fr 1fr;
}

.split__media {
  position: sticky;
  top: 0;
  align-self: start;
  height: 100dvh;
}

.split__media img {
	width: 100%;
	height: 100%;
	object-fit: cover;
	object-position: center;
}

.split__content {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 20px;
	padding-top: calc(var(--header-height) + 20px);
}

@media (max-width: 768px) {
	.split {
		grid-template-columns: 1fr;
	}

	.split__media {
		display: none;
	}
}
</style>
