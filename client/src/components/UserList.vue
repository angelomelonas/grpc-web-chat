<template>
    <v-container pa-2>
        <v-list
                style="max-height: 100%"
                class="scroll-y"
                :style="{ maxHeight: 480 + 'px' }"
        >
            <v-list-tile v-if="subscribedUserList == 0">
                No subscribed users
            </v-list-tile>
            <template
                    v-for="user in subscribedUserList"
            >
                <v-list-tile
                        :key="user"
                >
                    <v-icon :style="{color: user.subscribed ? '#a5d6a7' : '#ffab91'}">fiber_manual_record</v-icon>
                    <v-list-tile-content>
                        <v-list-tile-title v-text="user.username"></v-list-tile-title>
                    </v-list-tile-content>

                </v-list-tile>
            </template>
        </v-list>
    </v-container>
</template>

<script lang="ts">
    import {Component, Vue} from "vue-property-decorator";
    import orderBy from "lodash.orderby";
    import chat from "@/store/modules/chat";

    @Component({
        name: "UserList"
    })
    export default class TypeBox extends Vue {
        get subscribedUserList(): Array<object> {
            return orderBy(chat.getSubscribedUsersList, "subscribed", "desc");
        }
    }
</script>

<style scoped>
</style>
