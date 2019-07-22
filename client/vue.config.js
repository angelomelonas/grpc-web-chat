const fs = require("fs");

module.exports = {
  // Change build paths to make them Maven compatible
  // see https://cli.vuejs.org/config/
  outputDir: "target/dist",
  assetsDir: "static",
  devServer: {
    public:'0.0.0.0', // THIS IS FOR CORS
    host: "0.0.0.0",
    port: 443,
//    https: true,
    https: {
      key: fs.readFileSync("./pem/server.key"),
      cert: fs.readFileSync("./pem/server.pem"),
      ca: fs.readFileSync("./pem/ca.crt"),
    },
    disableHostCheck: true,
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, PATCH, OPTIONS",
      "Access-Control-Allow-Headers": "keep-alive,user-agent,cache-control,content-type,content-transfer-encoding,custom-header-1,x-accept-content-transfer-encoding,x-accept-response-streaming,x-user-agent,x-grpc-web,grpc-timeout"
    },
//    proxy: {
//      '^/': {
//        target: "localhost",
//        protocol: 'https:',
//        port: 8080,
//        ws: true,
//        changeOrigin: true,
//      },
//    },
  },
};
