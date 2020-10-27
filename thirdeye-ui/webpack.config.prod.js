const path = require("path");
const webpackConfigBase = require("./webpack.config.base");

// Get basic webpack configuration object
const webpackConfig = webpackConfigBase.getWebpackConfigObject(
    path.join(__dirname, "src/app/index.tsx"),
    path.join(__dirname, "dist"),
    "thirdeye-ui",
    path.join(__dirname, "node_modules"),
    false
);

// Set up a distributable index.html output along with other relevant assets (favicon, logo and
// manifest)
webpackConfigBase.setupDistributableIndexHtml(
    webpackConfig,
    path.join(__dirname, "src/public/index.html"),
    path.join(__dirname, "src/public/favicon.ico"),
    path.join(__dirname, "src/public/cortex-data-512x512.png"),
    path.join(__dirname, "src/public/manifest.json")
);

// Get webpack configuration function to configure webpack in production mode
module.exports = webpackConfigBase.getWebpackConfigProdFunction(webpackConfig);
