const path = require("path");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const ForkTsCheckerWebpackPlugin = require("fork-ts-checker-webpack-plugin");
const outputPath = path.join(__dirname, "dist");

module.exports = {
    // Input configuration
    entry: path.join(__dirname, "src/app/index.tsx"),

    // Output configuration
    output: {
        path: outputPath,
        filename: "thirdeye-ui.js",
        publicPath: "/", // Ensures bundle is served from absolute path as opposed to relative
        pathinfo: false, // Development mode optimization
    },

    // Loaders
    module: {
        rules: [
            {
                // .ts and .tsx files to be handled by ts-loader
                test: /\.(ts|tsx)$/,
                loader: "ts-loader",
                options: {
                    // Speed up compilation in development mode
                    transpileOnly: true,
                },
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
                // font files to be handled by file-loader
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
                exclude: /node_modules/, // Just the source code
            },
        ],
    },

    // Configure how modules are resolved
    resolve: {
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
            ".ttf",
        ],
    },

    // Plugins
    plugins: [
        // Clean webpack output directory
        new CleanWebpackPlugin({
            verbose: true,
        }),
        // In development mode, fork TypeScript checking to run in another thread and not block
        // main transpilation
        new ForkTsCheckerWebpackPlugin({
            eslint: {
                files: "./src/**/*.{ts,tsx}",
            },
        }),
        // Generate index.html from template
        new HtmlWebpackPlugin({
            template: path.join(__dirname, "src/public/index.html"),
            scriptLoading: "defer",
        }),
        // Copy favicon, logo and manifest for index.html
        new CopyWebpackPlugin({
            patterns: [
                {
                    from: path.join(__dirname, "src/public/favicon.ico"),
                    to: outputPath,
                },
                {
                    from: path.join(
                        __dirname,
                        "src/public/cortex-data-512x512.png"
                    ),
                    to: outputPath,
                },
                {
                    from: path.join(__dirname, "src/public/manifest.json"),
                    to: outputPath,
                },
            ],
        }),
    ],

    // Setup webpack-dev-server
    devServer: {
        contentBase: outputPath,
        compress: true,
        port: 7004,
        // Required to route all requests to index.html so that React router gets to handle all
        // copy pasted deep links
        historyApiFallback: {
            disableDotRule: true,
        },

        // Setup webpack-dev-server proxy context and target
        proxy: [
            {
                context: "/api",
                target: "http://localhost:8080",
                changeOrigin: true,
            },
        ],
    },

    // Development mode
    mode: "development",

    // Use eval-cheap-module-source-map for faster rebuilds
    devtool: "eval-cheap-module-source-map",

    // Development mode optimizations
    optimization: {
        runtimeChunk: true,
        removeAvailableModules: false,
        removeEmptyChunks: false,
        splitChunks: false,
    },
};
