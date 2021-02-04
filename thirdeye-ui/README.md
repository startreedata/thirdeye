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

#### Node Version Manager (nvm)

The project uses [nvm](https://github.com/nvm-sh/nvm) to maintain the Node version. Compatible Node version is listed in project root `.nvmrc`.

Once you install nvm, go to the project directory and switch to the compatible node version

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

#### Configure Node Package Manager (npm) for use with GitHub Packages

The project may depend on some GitHub Packages and [npm](https://www.npmjs.com) needs to be configured to allow access to these packages using a GitHub account. The GitHub repository to install packages from is configured in project root `.npmrc`.

Follow the official GitHub Docs [here](https://docs.github.com/en/free-pro-team@latest/packages/using-github-packages-with-your-projects-ecosystem/configuring-npm-for-use-with-github-packages#authenticating-with-a-personal-access-token) to configure [npm](https://www.npmjs.com) with a GitHub personal access token using `~/.npmrc` file.

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

This will build and deploy the project using webpack-dev-server at http://localhost:7004.

## Scripts

### `start`

Build and deploy the project using webpack-dev-server at http://localhost:7004

```
$ npm run start
```

### `build`

Build the project and output the bundles in project root `dist`

```
$ npm run build
```

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

Run all tests and generate coverage report in project root `src/test/coverage`

```
$ npm run test-coverage
```

### `eslint`

Run [ESLint](https://eslint.org) across the project except for files and directories listed in project root `.eslintignore`

```
$ npm run eslint
```

### `stylelint`

Run [stylelint](https://stylelint.io) across the project except for files and directories listed in project root `.stylelintignore`

```
$ npm run stylelint
```

### `pretty`

Run [Prettier](https://prettier.io) across the project except for files and directories listed in project root `.prettierignore`

```
$ npm run pretty
```
