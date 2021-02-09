const path = require("path");
const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const WebpackBar = require("webpackbar");
const webpack = require("webpack");

const outputPath = path.join(__dirname, "dist");

module.exports = {
    // Production mode
    mode: "production",

    // Input configuration
    entry: path.join(__dirname, "src/app/index.tsx"),

    // Output configuration
    output: {
        path: outputPath,
        filename: "thirdeye-ui.js",
        chunkFilename: "[name].js",
        publicPath: "/", // Ensures bundle is served from absolute path as opposed to relative
    },

    // Loaders
    module: {
        rules: [
            {
                // .ts and .tsx files to be handled by ts-loader
                test: /\.(ts|tsx)$/,
                loader: "ts-loader",
                exclude: /node_modules/, // Just the source code
            },
            {
                // .css and .scss files to be handled by sass-loader
                test: /\.(css|scss)$/,
                use: ["style-loader", "css-loader", "sass-loader"],
                // No exclude, may need to handle files outside the source code
                // (from node_modules)
            },
            {
                // .svg files to be handled by @svgr/webpack
                test: /\.svg$/,
                use: ["@svgr/webpack", "url-loader"],
                exclude: /node_modules/, // Just the source code
            },
            {
                // Font files to be handled by file-loader
                test: /\.ttf$/,
                use: [
                    {
                        loader: "file-loader",
                        options: {
                            name: "[name].[ext]",
                            outputPath: "assets/fonts/",
                        },
                    },
                ],
                exclude: /node_modules/, // Just the source code
            },
        ],
    },

    // Module resolution
    resolve: {
        // File types to be handled
        extensions: [".ts", ".tsx", ".js", ".css", ".scss", ".svg", ".ttf"],
    },

    plugins: [
        // Clean webpack output directory
        new CleanWebpackPlugin({
            verbose: true,
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
                        "src/public/thirdeye-512x512.png"
                    ),
                    to: outputPath,
                },
                {
                    from: path.join(__dirname, "src/public/manifest.json"),
                    to: outputPath,
                },
            ],
        }),
        // Build progress bar
        new WebpackBar({
            name: "@cortexdata/thirdeye-ui [prod]",
            color: "#6EC4D1",
        }),
        new webpack.ProvidePlugin({
            process: "process/browser",
        }),
    ],

    // Source map
    devtool: "source-map",
};
