# gRPC Web Chat (Work in Progress)
A simple project demonstrating how both a Go and Java back end can power the same Vue.js front end using gRPC. 

* The server consists either of a Java or Go Lang project.
    * The Java project uses the Spring Boot framework with Maven for dependency management.
    * _TODO: Go Lang project..._
* The client is written in TypeScript and uses the [VueJS framework](https://github.com/vuejs/vue). It also uses [Vuetify](https://github.com/vuetifyjs/vuetify) and the [official gRPC-Web library](https://github.com/grpc/grpc-web).
* The [Envoy Proxy](https://github.com/envoyproxy/envoy) is used to translate between the browser and the gRPC server.
    * TLS/SSL is used to allow HTTP2 connections between the browser and Envoy. This is to circumvent [browser connection limitations](https://docs.pushtechnology.com/cloud/latest/manual/html/designguide/solution/support/connection_limitations.html). More on this [here](https://github.com/grpc/grpc-web/issues/522).

## Overview

![gRPC-Web Overview](https://i.ibb.co/bWjrTzb/grpc-diagram.png)

## Project Branches
The root of the project contains the shared API and client code. Checkout the relevant server branch in your language of choice. 

```
master  -> dev-stable [-> dev]  -> go-dev-stable    [-> go-dev]
                                -> java-dev-stable  [-> java-dev]
```

## Requirements
##### Docker
1. Install Docker on your local machine.

##### Protoc
1. Go to [the releases page](https://github.com/protocolbuffers/protobuf/releases)  of [Protobuf](https://github.com/protocolbuffers/protobuf).
2. Select the latest release version.
3. Scroll down to `Assets` and download the applicable file (e.g., for Windows `protoc-3.8.0-rc-1-win64.zip`).
4. Extract the contents and add to your path (e.g., for Windows, simply add the `protoc.exe` to your path).

##### gRPC-Web Protoc Plugin
1. Go to [the releases page](https://github.com/grpc/grpc-web/releases)  of [gRPC-Web](https://github.com/grpc/grpc-web).
2. Select the latest release version.
3. Scroll down to `Assets` and download the applicable file (e.g., for Windows `protoc-gen-grpc-web-1.0.4-windows-x86_64.exe`).
4. Extract the `protoc-gen-grpc-web` file a directory and it to your path (e.g., for Windows add the `protoc-gen-grpc-web.exe` file to your path).

## Installation

### Server

#### Java
1. Checkout the `java-dev-stable` branch.
2. Execute `mvn clean install` in the root directory to build and compile the project. This will also generate all the necessary Protocol Buffer files for the backend and frontend.
3. Simply run the `ChatApplication.java` as a normal Java application. This will start the server. See the `resources/application.properties` file for server configuration details.

#### Go
1. Checkout the `go-dev-stable` branch.
2. _TODO: Implement..._

### Envoy Proxy
1. From the root directory, run the `create-cert.sh` script. See the `envoy/README.md` file for more details.
2. Run `docker-compose up` to start the Envoy proxy.

### Client
1. Run `npm install` and then `npm run proto` (NOTE: This step is unnecessary after running the `mvn` command for the Java server).
2. Run `npm run serve` to start the development server.
