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
  MessageRequest,
  MessageResponse,
  SubscriptionRequest,
  UnsubscriptionRequest,
  UnsubscriptionResponse
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
  appendMessage(timestamp: number, username: string, message: string) {
    this.messages +=
      "[" +
      moment(timestamp).format("LTS") +
      "] " +
      username +
      ": " +
      message +
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
    subscriptionRequest.setUsername(this.username);

    this.chatServiceClient
      .subscribe(subscriptionRequest)
      .on("data", message => {
        this.setSubscription(true);
        this.appendMessage(
          message.getTimestamp(),
          message.getUsername(),
          message.getMessage()
        );
      })
      .on("error", error => {
        this.setSubscription(false);
        console.error(error);
      })
      .on("end", () => {
        console.log("Chat session closed.");
      });
  }

  @Action
  unsubscribe() {
    const unsubscriptionRequest = new UnsubscriptionRequest();
    unsubscriptionRequest.setUuid(this.getSessionId);

    this.chatServiceClient.unsubscribe(
      unsubscriptionRequest,
      {},
      (err: grpcWeb.Error, unsubscriptionResponse: UnsubscriptionResponse) => {
        if (unsubscriptionResponse) {
          this.appendMessage(
            moment().unix(),
            "Server",
            unsubscriptionResponse.getMessage()
          );
          this.setSubscription(false);
          console.log("Client has been unsubscribed.");
        }
        if (err) {
          console.error(err);
        }
      }
    );
  }

  @Action
  sendMessage(message: string) {
    const messageRequest = new MessageRequest();

    messageRequest.setUsername(this.username);
    messageRequest.setMessage(message);

    this.chatServiceClient.sendMessage(
      messageRequest,
      {},
      (err: grpcWeb.Error, messageResponse: MessageResponse) => {
        if (messageResponse) {
          console.log("Message received: " + messageResponse.getMessage());
        }

        if (err) {
          console.error(err);
        }
      }
    );
  }
}

export default getModule(ChatModule);
