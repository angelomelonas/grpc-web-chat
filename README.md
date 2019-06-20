# gRPC-Web Chat Server in Go
A simple project demonstrating how both a Go and Java back end can power the same Vue.js front end using gRPC.

## Branches
The root of the project contains the shared API and client code. Check out the relevant server branch in your language of choice. 
```
master -> dev-stable -> go-dev-stable
                     -> java-dev-stable
```


## Flow

```
Client Request  -> Authenticate(AuthenticationRequest{})                # Client requests authentication.
Server Response -> AuthenticationResponse{uuid}                         # Server generates UUID, Client stores UUID.
    
Client Request  -> Subscribe(SubscriptionRequest{uuid, username}        # Client requests to subscribe.
Server Response -> Message{username, message, timestamp}                # Server responds with Message.

Client Request  -> SendMessage(MessageRequest{Message})                 # Client sends a message.
Server Response -> Message{...}                                         # Server echos back message.
Server Response -> Subscribe(Message)                                   # Server broadcasts Message to all clients.

Client Request  -> Unsubscribe(UnsubscriptionRequest{uuid})             # Client requests to unsubscribe.
Server Response -> Message{...}                                         # Server responds with Message.   
```
