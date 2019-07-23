The default setup works for `localhost`.

##### For Windows
1. Run `create-cert.sh` to create certificates.
2. Add `ca.crt` to you machine's Trusted Root Certificates.
3. From the root project directory, run `docker-compose up` to start Envoy.

##### For Linux/OSX
1. Modify the `create-cert.sh` by replacing double back slashes (`\\`) with single forward slash (`/`).
2. Run `create-cert.sh` to create certificates.
3. Add `ca.crt` to you machine's Trusted Root Certificates.
4. From the root project directory, run `docker-compose up` to start Envoy.
