<img align="right" width="65" height="65" src="./src/public/thirdeye-512x512.png">

# ThirdEye UI

[![ThirdEye UI Workflow](https://github.com/cortexdata/thirdeye/workflows/ThirdEye%20UI%20Workflow/badge.svg)](https://github.com/cortexdata/thirdeye/actions?query=workflow%3A%22ThirdEye+UI+Workflow%22)

<br/>

This is the project for CortexData ThirdEye UI.

-   [Getting Started](#getting-started)
    -   [Prerequisites](#prerequisites)
        -   [Node Version Manager (nvm)](#node-version-manager-nvm)
        -   [Configure Node Package Manager (npm) for use with GitHub Packages](#configure-node-package-manager-npm-for-use-with-github-packages)
    -   [Setup](#setup)
    -   [Run](#run)
-   [Supported Browsers](#supported-browsers)
-   [Scripts](#scripts)
    -   [`start`](#start)
    -   [`build`](#build)
    -   [`test`](#test)
    -   [`test-watch`](#test-watch)
    -   [`test-coverage`](#test-coverage)
    -   [`eslint`](#eslint)
    -   [`stylelint`](#stylelint)
    -   [`pretty`](#pretty)

## Getting Started

These instructions will help you get the project up and running on your local machine for development and testing purposes.

### Prerequisites

#### [Node Version Manager (nvm)](https://github.com/nvm-sh/nvm)

The project uses nvm to maintain the [Node](https://nodejs.org) version. Compatible Node version is listed in project root [**.nvmrc**](.nvmrc).

Once you install nvm, go to the project directory and switch to the compatible Node version

```
$ nvm use
```

This will switch to the required Node version if already installed and make `npm` command available in the terminal.

If required Node version is not installed, it will recommend the command to install it

```
Found '/Users/default/thirdeye/thirdeye-ui/.nvmrc' with version <14.7.0>
N/A: version "14.7.0 -> N/A" is not yet installed.

You need to run "nvm install 14.7.0" to install it before using it.
```

Following the installation, the command above will let you switch to the required Node version.

#### Configure [Node Package Manager (npm)](https://www.npmjs.com) for use with GitHub Packages

The project may depend on some GitHub Packages and npm needs to be configured to allow access to these packages using a GitHub account. The GitHub repository to install packages from is configured in project root [**.npmrc**](.npmrc).

Follow the official GitHub Docs [here](https://docs.github.com/en/free-pro-team@latest/packages/using-github-packages-with-your-projects-ecosystem/configuring-npm-for-use-with-github-packages#authenticating-with-a-personal-access-token) to configure npm with a GitHub personal access token using **~/.npmrc**.

### Setup

Once you clone the repository, go to the project directory and install

```
$ npm install
```

This will install necessary dependencies for the project.

### Run

Once set up, go to the project directory and run

```
$ npm run start
```

This will build and deploy the project using [webpack-dev-server](https://github.com/webpack/webpack-dev-server) at http://localhost:7004.

## Supported Browsers

CortexData ThirdEye UI is tested on latest, stable release of [Chrome](https://www.google.com/chrome), [Firefox](https://www.mozilla.org/firefox), [Safari](https://www.apple.com/safari) and [Edge](https://www.microsoft.com/edge).

## Scripts

### `start`

Build and deploy the project using [webpack-dev-server](https://github.com/webpack/webpack-dev-server) at http://localhost:7004

```
$ npm run start
```

### `build`

Build the project and output the bundles in project root **dist**

```
$ npm run build
```

This will also analyze the bundles using [Webpack Bundle Analyzer](https://github.com/webpack-contrib/webpack-bundle-analyzer) and generate bundle report in project root **webpack**.

### `test`

Run all tests

```
$ npm run test
```

### `test-watch`

Watch files for changes and re-run tests related to changed files

```
$ npm run test-watch
```

### `test-coverage`

Run all tests and generate coverage report in project root **src/test/unit/coverage**

```
$ npm run test-coverage
```

### `eslint`

Run [ESLint](https://eslint.org) across the project except for files and directories listed in project root [**.eslintignore**](.eslintignore)

```
$ npm run eslint
```

### `stylelint`

Run [stylelint](https://stylelint.io) across the project except for files and directories listed in project root [**.stylelintignore**](.stylelintignore)

```
$ npm run stylelint
```

### `pretty`

Run [Prettier](https://prettier.io) across the project except for files and directories listed in project root [**.prettierignore**](.prettierignore)

```
$ npm run pretty
```
