const path = require("path");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const ForkTsCheckerNotifierWebpackPlugin = require("fork-ts-checker-notifier-webpack-plugin");
const ForkTsCheckerWebpackPlugin = require("fork-ts-checker-webpack-plugin");

/**
 * Returns a basic webpack configuration object required by all the projects
 * @param entryPath path at which application starts executing and webpack starts bundling
 * @param outputPath target directory for all output files
 * @param outputBundleName filename for output bundle (without .js extension)
 * @param nodeModulesPath project node_modules directory
 * @param isDev flag to enable development mode optimizations
 */
module.exports.getWebpackConfigObject = (
    entryPath,
    outputPath,
    outputBundleName,
    nodeModulesPath,
    isDev = false
) => {
    const webpackConfig = {};

    // Input configuration
    webpackConfig.entry = entryPath;

    // Output configuration
    webpackConfig.output = {
        path: outputPath,
        filename: outputBundleName + ".js", // Required to deploy the bundle on a web server
        library: outputBundleName, // Required to include the bundle as a library in another project
        libraryTarget: "umd", // Required to include the bundle as a library in another project
        publicPath: "/", // Ensures bundle is served from absolute path as opposed to relative
    };

    // Loaders
    webpackConfig.module = {
        rules: [
            {
                // .ts and .tsx files to be handled by ts-loader
                test: /\.(ts|tsx)$/,
                loader: "ts-loader",
                options: isDev
                    ? {
                          transpileOnly: true,
                      }
                    : undefined,
                exclude: /node_modules/, // Just the source code
            },
            {
                // .css and .scss files to be handled by sass-loader
                test: /\.(css|scss)$/,
                use: ["style-loader", "css-loader", "sass-loader"],
                // Missing exclude, may need to handle files outside the source code
                // (from node_modules)
            },
            {
                // .svg files to be handled by @svgr/webpack
                test: /\.svg$/,
                use: ["@svgr/webpack", "url-loader"],
                exclude: /node_modules/, // Just the source code
            },
            {
                test: /\.(woff|woff2|ttf)([\?]?.*)$/,
                use: [
                    {
                        loader: "file-loader",
                        options: {
                            name: "[name].[ext]",
                            outputPath: "fonts/",
                        },
                    },
                ],
            },
        ],
    };

    // Configure how modules are resolved
    webpackConfig.resolve = {
        // File types to be handled
        extensions: [
            ".ts",
            ".tsx",
            ".js",
            ".css",
            ".scss",
            ".svg",
            ".woff",
            ".woff2",
        ],
        alias: {
            // Force all common-ui peerDependencies to always be resolved to the node_modules
            // directory of the dependent project
            // This is required when dependent project depends on common-ui using relative file
            // path ("@cortexdata/common-ui": "file:../common-ui") in package.json
            // Since all common-ui peerDependencies are also its devDependencies, the dependent
            // project ends up with two instances of such dependencies and confusion ensues
            // This is one aspect of using common-ui as an external component library, another
            // being webpack externals configuration in common-ui/webpack.config.prod.js
            "@material-ui": path.join(nodeModulesPath, "@material-ui"),
            react: path.join(nodeModulesPath, "react"),
            "react-dom": path.join(nodeModulesPath, "react-dom"),
            "@hookform/resolvers": path.join(
                nodeModulesPath,
                "@hookform/resolvers"
            ),
            "react-hook-form": path.join(nodeModulesPath, "react-hook-form"),
            i18next: path.join(nodeModulesPath, "i18next"),
            yup: path.join(nodeModulesPath, "yup"),
        },
    };

    // Plugins
    webpackConfig.plugins = [
        // Clean webpack output directory
        new CleanWebpackPlugin({
            verbose: true,
        }),
    ];

    // If dev mode, fork ts checking to run in another thread and not block main transpilation
    // https://webpack.js.org/guides/build-performance/#typescript-loader
    // https://github.com/TypeStrong/ts-loader/blob/master/examples/fork-ts-checker-webpack-plugin/webpack.config.development.js
    if (isDev) {
        webpackConfig.plugins.push(
            new ForkTsCheckerWebpackPlugin({
                eslint: {
                    files: "./src/**/*.{ts,tsx,js,jsx}",
                },
            })
        );
        webpackConfig.plugins.push(
            new ForkTsCheckerNotifierWebpackPlugin({
                title: "TypeScript",
                excludeWarnings: false,
            })
        );
    }

    return webpackConfig;
};

/**
 * Allows projects to update given webpack configuration object to set up a distributable
 * index.html output along with other relevant assets (favicon, logo and manifest)
 * @param webpackConfig webpack configuration object to set up a distributable index.html output
 * @param indexHtmlPath template for output index.html
 * @param faviconPath favicon for output index.html
 * @param logoPath logo for output index.html
 * @param manifestPath manifest for output index.html
 */
module.exports.setupDistributableIndexHtml = (
    webpackConfig,
    indexHtmlPath,
    faviconPath,
    logoPath,
    manifestPath
) => {
    // Setup plugins
    webpackConfig.plugins = webpackConfig.plugins || [];

    // Generate index.html from template
    webpackConfig.plugins.push(
        new HtmlWebpackPlugin({
            template: indexHtmlPath,
            scriptLoading: "defer",
        })
    );

    // Copy favicon, logo and manifest for index.html
    webpackConfig.plugins.push(
        new CopyWebpackPlugin({
            patterns: [
                { from: faviconPath, to: webpackConfig.outputPath },
                { from: logoPath, to: webpackConfig.outputPath },
                { from: manifestPath, to: webpackConfig.outputPath },
            ],
        })
    );
};

/**
 * Allows projects to update given webpack configuration object to set up a webpack-dev-server
 * proxy
 * @param webpackConfig webpack configuration object to set up a webpack-dev-server proxy
 * @param context URL context to determine which requests to be proxied to the target host
 * @param targetHost target host
 */
module.exports.setupWebpackDevServerProxy = (
    webpackConfig,
    context,
    targetHost
) => {
    // Setup webpack-dev-server
    webpackConfig.devServer = webpackConfig.devServer || {};

    // Setup webpack-dev-server proxy
    webpackConfig.devServer.proxy = webpackConfig.devServer.proxy || {};

    // Setup proxy context and target
    webpackConfig.devServer.proxy[context] = {
        target: targetHost,
        changeOrigin: true,
    };
};

/**
 * Returns a webpack configuration function (not object) that helps projects configure webpack in
 * development mode based on appropriate environment verification
 * @param webpackConfig webpack configuration object to configure webpack in development mode
 * @param localPort port number for webpack-dev-server
 */
module.exports.getWebpackConfigDevFunction = (webpackConfig, localPort) => {
    return (env) => {
        // Log mode
        console.log("webpack mode: ", env);

        // Stop if not in development mode
        if (!env.development) {
            throw console.error("webpack mode: ", {
                error:
                    "development configuration function being used in wrong environment",
            });
        }

        // In development environment, include source maps for better debugging
        webpackConfig.mode = "development";

        // Use eval-cheap-module-source-map for faster rebuilds
        // https://webpack.js.org/guides/build-performance/#devtool
        // https://webpack.js.org/configuration/devtool/
        webpackConfig.devtool = "eval-cheap-module-source-map";

        // Optimizations
        // https://webpack.js.org/guides/build-performance/#minimal-entry-chunk
        // https://webpack.js.org/guides/build-performance/#avoid-extra-optimization-steps
        webpackConfig.optimization = {
            runtimeChunk: true,
            removeAvailableModules: false,
            removeEmptyChunks: false,
            splitChunks: false,
        };

        // https://webpack.js.org/guides/build-performance/#output-without-path-info
        webpackConfig.output.pathinfo = false;

        // Setup webpack-dev-server
        webpackConfig.devServer = webpackConfig.devServer || {};

        webpackConfig.devServer.contentBase = webpackConfig.output.path;
        webpackConfig.devServer.compress = true;
        webpackConfig.devServer.port = localPort;
        // Required to route all requests to index.html so that React router gets to handle all
        // copy pasted deep links
        webpackConfig.devServer.historyApiFallback = { disableDotRule: true };

        // Log proxy information
        const proxyConfig = webpackConfig.devServer.proxy || {
            disabled: true,
        };
        console.log("webpack-dev-server proxy: ", proxyConfig);

        return webpackConfig;
    };
};

/**
 * Returns a webpack configuration function (not object) that helps projects configure webpack in
 * production mode based on appropriate environment verification
 * @param webpackConfig webpack configuration object to configure webpack in production mode
 */
module.exports.getWebpackConfigProdFunction = (webpackConfig) => {
    return (env) => {
        // Stop if not in production mode
        if (!env.production) {
            throw console.error("webpack mode: ", {
                error:
                    "production configuration function being used in wrong environment",
            });
        }

        // In production environment, include cheap source maps for potential debugging
        webpackConfig.mode = "production";
        webpackConfig.devtool = "cheap-source-map";

        return webpackConfig;
    };
};
