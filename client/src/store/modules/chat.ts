import {
  Action,
  getModule,
  Module,
  Mutation,
  VuexModule
} from "vuex-module-decorators";
import store from "@/store";
import { ChatServiceClient } from "../../../proto/chat_grpc_web_pb";
import {
  AuthenticationRequest,
  AuthenticationResponse,
  Message,
  MessageRequest,
  MessageResponse,
  SubscribedUsers,
  SubscribedUsersRequest,
  SubscriptionRequest,
  UnsubscriptionRequest,
  UnsubscriptionResponse,
  User
} from "../../../proto/chat_pb";
import * as grpcWeb from "grpc-web";
import moment from "moment";

@Module({
  namespaced: true,
  name: "chat",
  store,
  dynamic: true
})
class ChatModule extends VuexModule {
  chatServiceClient!: ChatServiceClient;

  sessionId: string = "";
  subscribed: boolean = false;
  username: string = "";
  messages: string = "";

  subscribedUserList: Array<User> = [];

  get getSessionId(): string {
    return this.sessionId;
  }

  get getUsername(): string {
    return this.username;
  }

  get getMessages(): string {
    return this.messages;
  }

  get isSubscribed(): boolean {
    return this.subscribed;
  }

  get getSubscribedUsersList(): Array<User> {
    return this.subscribedUserList;
  }

  @Mutation
  setSessionId(sessionId: string) {
    this.sessionId = sessionId;
  }

  @Mutation
  setSubscription(subscription: boolean) {
    this.subscribed = subscription;
  }

  @Mutation
  setUsername(username: string) {
    this.username = username;
  }

  @Mutation
  setSubscribedUsersList(subscribedUserList: Array<User>) {
    this.subscribedUserList = subscribedUserList;
  }

  @Mutation
  appendMessage(message: {
    timestamp: number;
    username: string;
    message: string;
  }) {
    this.messages +=
      "[" +
      moment(message.timestamp).format("LTS") +
      "] " +
      message.username +
      ": " +
      message.message +
      "\n";
  }

  @Action
  connectClient(host: { hostname: string; port: number }) {
    this.chatServiceClient = new ChatServiceClient(
      "http://" + host.hostname + ":" + host.port,
      null,
      null
    );
  }

  @Action
  authenticate() {
    const authenticationRequest = new AuthenticationRequest();

    this.chatServiceClient.authenticate(
      authenticationRequest,
      {},
      (err: grpcWeb.Error, authenticationResponse: AuthenticationResponse) => {
        if (!err) {
          this.setSessionId(authenticationResponse.getUuid());
        } else {
          console.error(err);
        }
      }
    );
  }

  @Action
  subscribe() {
    const subscriptionRequest = new SubscriptionRequest();

    subscriptionRequest.setUuid(this.getSessionId);
    subscriptionRequest.setUsername(this.getUsername);
    this.chatServiceClient
      .subscribe(subscriptionRequest)
      .on("data", (message: Message) => {
        if (!this.isSubscribed) {
          this.setSubscription(true);

          // Subscribe to the list of subscribed users.
          this.subscribedUsersList();
        }

        this.appendMessage({
          timestamp: message.getTimestamp(),
          username: message.getUsername(),
          message: message.getMessage()
        });
      })
      .on("error", (error: grpcWeb.Error) => {
        this.setSubscription(false);
        this.appendMessage({
          timestamp: moment().unix(),
          username: "Server",
          message: error.message
        });
        console.error(error);
      })
      .on("end", () => {
        console.log("Unsubscribed from chat session.");
      });
  }

  @Action
  unsubscribe() {
    const unsubscriptionRequest = new UnsubscriptionRequest();
    unsubscriptionRequest.setUuid(this.getSessionId);

    this.chatServiceClient.unsubscribe(
      unsubscriptionRequest,
      {},
      (
        error: grpcWeb.Error,
        unsubscriptionResponse: UnsubscriptionResponse
      ) => {
        if (unsubscriptionResponse) {
          this.appendMessage({
            timestamp: moment().unix(),
            username: "Server",
            message: unsubscriptionResponse.getMessage()
          });
        }
        if (error) {
          this.appendMessage({
            timestamp: moment().unix(),
            username: "Server",
            message: error.message
          });
          console.error(error);
        }
        this.setSubscription(false);
      }
    );
  }

  @Action
  sendMessage(message: string) {
    const messageRequest = new MessageRequest();

    messageRequest.setUuid(this.getSessionId);
    messageRequest.setUsername(this.getUsername);
    messageRequest.setMessage(message);

    this.chatServiceClient.sendMessage(
      messageRequest,
      {},
      (error: grpcWeb.Error, messageResponse: MessageResponse) => {
        if (messageResponse) {
          console.log(
            "Message received on server: " + messageResponse.getMessage()
          );
        }

        if (error) {
          console.error(error);
        }
      }
    );
  }

  @Action
  subscribedUsersList() {
    const subscribedUsersRequest = new SubscribedUsersRequest();

    subscribedUsersRequest.setUuid(this.getSessionId);

    this.chatServiceClient
      .subscribedUserList(subscribedUsersRequest)
      .on("data", (subscribedUsers: SubscribedUsers) => {
        this.setSubscribedUsersList(subscribedUsers.getUsersList());
      })
      .on("error", (error: grpcWeb.Error) => {
        console.error(error);
      })
      .on("end", () => {
        console.log("Chat session closed.");
      });
  }
}

export default getModule(ChatModule);
