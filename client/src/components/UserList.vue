<template>
  <v-container>
    <v-list
      style="max-height: 100%"
      class="overflow-y-auto"
      :style="{ maxHeight: 480 + 'px' }"
    >
      <v-list-item v-if="!isSubscribed">
        Subscribe to see connected users
      </v-list-item>

      <v-list-item v-else-if="isSubscribed && subscribedUserList == 0">
        No other connected users
      </v-list-item>

      <template v-else v-for="user in subscribedUserList">
        <v-list-item
          v-if="user.getUsername() != username"
          :key="user.getUsername()"
        >
          <v-icon :style="{ color: '#a5d6a7' }">fiber_manual_record</v-icon>
          <v-list-item-content>
            <v-list-item-title v-text="user.getUsername()"></v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </template>
    </v-list>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import chat from "@/store/modules/chat";
import { User } from "../../proto/chat_pb";

@Component({
  name: "UserList"
})
export default class TypeBox extends Vue {
  get subscribedUserList(): Array<User> {
    return chat.getSubscribedUsersList;
  }

  get isSubscribed(): boolean {
    return chat.isSubscribed;
  }

  get username(): string {
    return chat.getUsername;
  }
}
</script>

<style scoped></style>
