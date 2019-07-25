<template>
  <v-container>
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="10">
        <v-textarea
          v-model="message"
          no-resize
          single-line
          flat
          filled
          autofocus
          height="96"
          counter="512"
          maxlength="512"
          label="Type message here..."
          :disabled="!isSubscribed"
          @keydown.enter.exact.prevent
          @keyup.enter.exact="
            sendMessage(message);
            clearMessage();
          "
        />
      </v-col>

      <v-col align-self="start" cols="2">
        <v-spacer />
        <v-btn
          color="primary"
          :disabled="!isSubscribed || message.length < 1"
          large
          @click="
            sendMessage(message);
            clearMessage();
          "
        >
          Send
        </v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import chat from "@/store/modules/chat";

@Component({
  name: "TypeBox"
})
export default class TypeBox extends Vue {
  private message: string = "";

  clearMessage(): void {
    this.message = "";
  }

  sendMessage(message: string): void {
    chat.sendMessage(message);
  }

  get isSubscribed(): boolean {
    return chat.isSubscribed;
  }
}
</script>

<style></style>
