module.exports = {
    // Project name
    displayName: "@cortexdata/thirdeye-ui",

    // Working directory
    roots: ["<rootDir>/src/app"],

    // Test coverage
    coverageDirectory: "<rootDir>/src/test/coverage",
    collectCoverageFrom: [
        "<rootDir>/src/app/**/*.{ts,tsx}", // All subdirectories under src/app
        "!<rootDir>/src/app/*.{ts,tsx}", // No files directly under src/app
    ],

    // Test files
    testMatch: ["<rootDir>/src/app/**/*.test.{ts,tsx}"],

    // TypeScript
    preset: "ts-jest",

    // Test Environment
    testEnvironment: "jest-environment-jsdom-fourteen",
    setupFilesAfterEnv: ["@testing-library/jest-dom/extend-expect"],
    moduleNameMapper: {
        "\\.svg": "<rootDir>/src/test/mocks/svgr-mock.js", // Mock SVG imports
        "\\.(css|scss)$": "identity-obj-proxy", // Mock stylesheet imports
    },
};
