const fs = require("fs");

module.exports = {
  // Change build paths to make them Maven compatible
  // see https://cli.vuejs.org/config/
  outputDir: "target/dist",
  assetsDir: "static",
  devServer: {
    host: "0.0.0.0",
    port: 443,
//    https: true,
    https: {
      key: fs.readFileSync("./pem/server.key"),
      cert: fs.readFileSync("./pem/server.crt"),
      ca: fs.readFileSync("./pem/ca.crt"),
    },
    disableHostCheck: true,
//    proxy: {
//      '^/': {
//        target: "0.0.0.0",
//        ws: true,
//        changeOrigin: true,
//      },
//    },
  },
};
