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
      key: fs.readFileSync("./certificates/server.key"),
      cert: fs.readFileSync("./certificates/server.crt")
    },
    disableHostCheck: true
  }
};
