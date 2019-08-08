<template>
  <v-container class="pa-2 pb-1">
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="10">
        <v-text-field
          label="Username"
          placeholder="Your username here..."
          filled
          maxlength="64"
          minlength="4"
          hide-details
          autofocus
          prepend-icon="account_circle"
          :value="username"
          :disabled="isSubscribed"
          @input="setUsername"
          @keydown.enter.exact.prevent
          @keyup.enter.exact="subscribe()"
        />
      </v-col>

      <v-col cols="2">
        <v-btn
          v-if="!isSubscribed"
          color="success"
          :disabled="username.length < 4"
          large
          @click="subscribe()"
        >
          Subscribe
        </v-btn>

        <v-btn
          v-else
          color="error"
          :disabled="username.length < 4 || username.length > 64"
          large
          @click="unsubscribe()"
        >
          Unsubscribe
        </v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import chat from "@/store/modules/chat";

@Component({
  name: "ConnectBox"
})
export default class TypeBox extends Vue {
  subscribe(): void {
    chat.subscribe();
  }

  unsubscribe(): void {
    chat.unsubscribe();
  }

  setUsername(username: string) {
    chat.setUsername(username);
  }

  get username(): string {
    return chat.getUsername;
  }

  get isSubscribed(): boolean {
    return chat.isSubscribed;
  }
}
</script>

<style></style>
