# Setting up the Envoy Proxy

The Envoy Proxy is configured in the `envoy.yaml` file. The default setup is for `localhost`.

##### Instructions for Creating Certificates
1. Run `create-ca-cert.sh` to create the Certificate Authority Root.
2. Run `create-cert.sh` to create server certificates, which use the CA Root created in the previous step.
3. Add the generated `ca.crt` (from the `keystore` directory) to you machine's Trusted Root Certificates (e.g., for Windows: [Manage Trusted Root Certificates in Windows 10/8](https://www.thewindowsclub.com/manage-trusted-root-certificates-windows)).
4. From the root project directory, run `docker-compose up` to start Envoy.
