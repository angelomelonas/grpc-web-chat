<template>
  <v-app>
    <v-toolbar dark class="primary" app dense flat clipped-left>
      <v-toolbar-title> gRPC CHAT</v-toolbar-title>
      <v-toolbar-items class="hidden-xs-only"></v-toolbar-items>
    </v-toolbar>

    <v-content fluid>
      <v-container>
        <connect-box></connect-box>
        <v-divider></v-divider>
        <chat-box></chat-box>
        <type-box></type-box>
      </v-container>
    </v-content>
  </v-app>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import ChatBox from "@/components/ChatBox.vue";
import TypeBox from "@/components/TypeBox.vue";
import ConnectBox from "@/components/ConnectBox.vue";
import chat from "@/store/modules/chat";

@Component({
  name: "App",
  components: { ConnectBox, ChatBox, TypeBox }
})
export default class App extends Vue {
  private created(): void {
    // On app creation, connect to the gRPC server.
    chat.connectClient({ hostname: "localhost", port: 8080 });

    // If the window is closed or reloaded, unsubscribe.
    window.addEventListener("beforeunload", this.onClose);
  }

  private destroyed(): void {
    chat.unsubscribe();
  }

  private onClose(): void {
    chat.unsubscribe();
  }
}
</script>

<style>
/* Styles here will be globally applied. */
html {
  overflow-y: hidden !important;
}
</style>
