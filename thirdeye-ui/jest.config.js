/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
        "\\.(svg|ttf)": "<rootDir>/src/test/unit/mocks/svg.mock.js", // Mock SVG imports
        "\\.(css|scss)$": "identity-obj-proxy", // Mock style imports
    },
};
