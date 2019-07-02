<template>
  <v-container pa-2>
    <v-list
      style="max-height: 100%"
      class="scroll-y"
      :style="{ maxHeight: 480 + 'px' }"
    >
      <v-list-tile v-if="!isSubscribed">
        Subscribe to see connected users
      </v-list-tile>

      <v-list-tile v-else-if="isSubscribed && subscribedUserList == 0">
        No other connected users
      </v-list-tile>

      <template v-else v-for="user in subscribedUserList">
        <v-list-tile
          v-if="user.getUsername() != username"
          :key="user.getUsername()"
        >
          <v-icon
            :style="{ color: user.getSubscribed() ? '#a5d6a7' : '#ffab91' }"
            >fiber_manual_record</v-icon
          >
          <v-list-tile-content>
            <v-list-tile-title v-text="user.getUsername()"></v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
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
