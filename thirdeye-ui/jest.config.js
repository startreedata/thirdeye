module.exports = {
    // Project name
    displayName: "@startree-ui/thirdeye-ui",

    // Working directory
    roots: ["<rootDir>/src/app"],

    // Test files
    testMatch: ["<rootDir>/src/app/**/*.test.{ts,tsx}"], // All test files in subdirectories under src/app

    // Test coverage
    coverageDirectory: "<rootDir>/src/test/unit/coverage",
    collectCoverageFrom: [
        "<rootDir>/src/app/**/*.{ts,tsx}", // All files in subdirectories under src/app
        "!<rootDir>/src/app/*", // Exclude files directly under src/app
        "!<rootDir>/src/app/**/index.{ts,tsx}", // Exclude index files
        "!<rootDir>/src/app/locale/**", // Exclude locale files
        "!<rootDir>/src/app/utils/material-ui/**", // Exclude Material-UI theme files
        "!<rootDir>/src/app/rest/dto/**", // Exclude backend DTOs
    ],

    // TypeScript
    preset: "ts-jest",

    // Test Environment
    testEnvironment: "jest-environment-jsdom-fourteen",
    setupFilesAfterEnv: ["@testing-library/jest-dom/extend-expect"],
    clearMocks: true,
    moduleNameMapper: {
        "\\.svg": "<rootDir>/src/test/unit/mocks/svg.mock.js", // Mock SVG imports
        "\\.(css|scss)$": "identity-obj-proxy", // Mock style imports
    },
};
