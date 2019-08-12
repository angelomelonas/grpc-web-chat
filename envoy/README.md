# Setting up the Envoy Proxy

The Envoy Proxy is configured in the `envoy.yaml` file. The default setup is for `localhost`.

##### For Windows
1. Run `create-cert.sh` to create certificates.
2. Add `ca.crt` to you machine's Trusted Root Certificates ([Manage Trusted Root Certificates in Windows 10/8](https://www.thewindowsclub.com/manage-trusted-root-certificates-windows)).
3. From the root project directory, run `docker-compose up` to start Envoy.

##### For Linux/OSX
1. Modify the `create-cert.sh` by replacing double back slashes (`\\`) with single forward slash (`/`).
2. Run `create-cert.sh` to create certificates.
3. Add `ca.crt` to you machine's Trusted Root Certificates.
4. From the root project directory, run `docker-compose up` to start Envoy.
