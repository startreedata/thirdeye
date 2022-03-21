const path = require("path");
const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const ForkTsCheckerWebpackPlugin = require("fork-ts-checker-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const WebpackBar = require("webpackbar");

const outputPath = path.join(__dirname, "dist");

module.exports = {
    // Development mode
    mode: "development",

    // Input configuration
    entry: path.join(__dirname, "src/app/index.tsx"),

    // Output configuration
    output: {
        path: outputPath,
        filename: "[name].js",
        chunkFilename: "[name].js",
        publicPath: "/", // Ensures bundle is served from absolute path as opposed to relative
    },

    // Loaders
    module: {
        rules: [
            // .ts and .tsx files to be handled by ts-loader
            {
                test: /\.(ts|tsx)$/,
                loader: "ts-loader",
                options: {
                    transpileOnly: true, // Speed up compilation in development mode
                },
            },
            // .css and .scss files to be handled by sass-loader
            {
                test: /\.(css|scss)$/,
                use: ["style-loader", "css-loader", "sass-loader"],
                // No exclude, may need to handle files outside the source code
                // (from node_modules)
            },
            // .svg files to be handled by @svgr/webpack
            {
                test: /\.svg$/,
                use: ["@svgr/webpack", "url-loader"],
            },
            // Font files to be handled by file-loader
            {
                test: /\.ttf$/,
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
        // In development mode, fork TypeScript checking to run in another thread and not block main
        // transpilation
        new ForkTsCheckerWebpackPlugin(),
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
            name: "@startree-ui/thirdeye-ui [dev]",
            color: "#54BAC9",
        }),
    ],

    // webpack-dev-server
    devServer: {
        contentBase: outputPath,
        compress: true,
        port: 7004,
        // Route all requests to index.html so that app gets to handle all copy pasted deep links
        historyApiFallback: {
            disableDotRule: true,
        },
        // Proxy configuration
        proxy: [
            {
                context: "/api",
                target: "http://23.99.9.151:8080/",
                changeOrigin: true,
            },
        ],
    },

    // Source map
    devtool: "eval-cheap-source-map",
};
