<template>
  <v-app>
    <v-app-bar dark app dense flat>
      <v-toolbar-title> gRPC CHAT</v-toolbar-title>
      <v-toolbar-items class="hidden-xs-only"></v-toolbar-items>
    </v-app-bar>

    <v-content>
      <v-container>
        <connect-box></connect-box>
        <v-divider></v-divider>
        <v-row class="fill-height" align="start" justify="center">
          <v-col cols="10">
            <chat-box></chat-box>
          </v-col>
          <v-col cols="2">
            <user-list></user-list>
          </v-col>
        </v-row>
        <type-box></type-box>
      </v-container>
    </v-content>

    <v-footer app> </v-footer>
  </v-app>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import ChatBox from "@/components/ChatBox.vue";
import TypeBox from "@/components/TypeBox.vue";
import ConnectBox from "@/components/ConnectBox.vue";
import chat from "@/store/modules/chat";
import UserList from "@/components/UserList.vue";

@Component({
  name: "App",
  components: { UserList, ConnectBox, ChatBox, TypeBox }
})
export default class App extends Vue {
  private created(): void {
    // On app creation, connect to the gRPC server via the proxy.
    chat.connectClient({ hostname: "localhost", port: 8080 });

    // Authenticate the client.
    chat.authenticate();
  }
}
</script>

<style>
/* Styles here will be globally applied. */
html {
  overflow-y: hidden !important;
}
</style>
