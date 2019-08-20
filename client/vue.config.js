const fs = require("fs");
const webpack = require("webpack");
const BundleAnalyzerPlugin = require("webpack-bundle-analyzer")
  .BundleAnalyzerPlugin;

module.exports = {
  // Change build paths to make them Maven compatible
  // see https://cli.vuejs.org/config/
  outputDir: "target/dist",
  assetsDir: "static",
  devServer: {
    // This is ONLY required for the dev server.
    host: "localhost",
    port: 443,
    https: {
      key: fs.readFileSync("./certificates/server.key"),
      cert: fs.readFileSync("./certificates/server.crt")
    },
    disableHostCheck: true
  },
  configureWebpack: {
    plugins: [
      new webpack.IgnorePlugin({
        resourceRegExp: /^\.\/locale$/,
        contextRegExp: /moment$/
      })
    ]
  }
};
