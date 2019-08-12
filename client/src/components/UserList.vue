<template>
  <v-list
    style="max-height: 100%"
    class="overflow-y-auto"
    :style="{ maxHeight: 480 + 'px' }"
    :disabled="!isSubscribed"
  >
    <v-list-item v-if="!isSubscribed" disabled>
      Subscribe to see connected users
    </v-list-item>

    <v-list-item v-else-if="isSubscribed && subscribedUserList == 0">
      No other connected users
    </v-list-item>

    <template v-else v-for="user in subscribedUserList">
      <v-list-item :key="user.getUsername()">
        <v-icon class="pr-1" :style="{ color: '#a5d6a7' }"
          >fiber_manual_record</v-icon
        >
        <v-list-item-content>
          <v-list-item-title
            v-if="user.getUsername() == username"
            v-text="user.getUsername() + ' (you)'"
          ></v-list-item-title>
          <v-list-item-title
            v-else
            v-text="user.getUsername()"
          ></v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </template>
  </v-list>
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

<style scoped>
.v-list:hover {
  outline: none;
  border-color: var(--v-primary-base);
  box-shadow: 0 0 1px var(--v-primary-base);
}

.v-list {
  background: rgb(250, 250, 250);
}
</style>
