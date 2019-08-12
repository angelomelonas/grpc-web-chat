<template>
  <v-textarea
    v-model="messages"
    filled
    hide-details
    clearable
    no-resize
    single-line
    flat
    height="480"
    name="input-7-4"
    readonly
  />
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import chat from "@/store/modules/chat";

@Component({
  name: "ChatBox"
})
export default class TypeBox extends Vue {
  get messages(): string {
    return chat.getMessages;
  }
}
</script>

<style></style>
