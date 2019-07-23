const fs = require("fs");

module.exports = {
  // Change build paths to make them Maven compatible
  // see https://cli.vuejs.org/config/
  outputDir: "target/dist",
  assetsDir: "static",
  devServer: {
    host: "localhost",
    port: 443,
    https: {
      key: fs.readFileSync("./pem/server.key"),
      cert: fs.readFileSync("./pem/server.crt")
    },
    disableHostCheck: true
  }
};
