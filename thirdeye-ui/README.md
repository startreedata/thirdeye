<img align="right" width="65" height="65" src="./src/public/thirdeye-512x512.png">

# ThirdEye UI

[![build status](https://ci.startreedata.io/api/v1/teams/startree-ui-projects/pipelines/thirdeye-ui-master/jobs/thirdeye-ui-merge/badge)](https://ci.startreedata.io/teams/startree-ui-projects/pipelines/thirdeye-ui-master/jobs/thirdeye-ui-merge) [![publish status](https://ci.startreedata.io/api/v1/teams/startree-ui-publish/pipelines/thirdeye-ui-publish-master/jobs/thirdeye-ui-publish/badge?title=publish)](https://ci.startreedata.io/teams/startree-ui-publish/pipelines/thirdeye-ui-publish-master/jobs/thirdeye-ui-publish)

<br/>

This is the UI project for StarTree ThirdEye.

-   [Getting started](#getting-started)
    -   [Prerequisites](#prerequisites)
        -   [Node Version Manager (nvm)](#node-version-manager-nvm)
        -   [Configure Node Package Manager (npm) for use with Artifactory](#configure-node-package-manager-npm-for-use-with-artifactory)
    -   [Setup](#setup)
    -   [Run](#run)
-   [Supported browsers](#supported-browsers)
-   [Scripts](#scripts)
    -   [`start`](#start)
    -   [`build`](#build)
    -   [`test`](#test)
    -   [`test-watch`](#test-watch)
    -   [`test-coverage`](#test-coverage)
    -   [`test-e2e`](#test-e2e)
    -   [`test-e2e-gui`](#test-e2e-gui)
    -   [`license-header-check`](#license-header-check)
    -   [`license-header-fix`](#license-header-fix)
    -   [`eslint-check`](#eslint-check)
    -   [`eslint-fix`](#eslint-fix)
    -   [`stylelint-check`](#stylelint-check)
    -   [`stylelint-fix`](#stylelint-fix)
    -   [`prettier-check`](#prettier-check)
    -   [`prettier-fix`](#prettier-fix)
    -   [`lint-check`](#lint-check)
    -   [`lint-fix`](#lint-fix)
    -   [`ci-check`](#ci-check)
-   [Contributing](#contributing)

## Getting started

These instructions will help you get the project up and running on your local machine for development and testing purposes.

### Prerequisites

#### [Node Version Manager (nvm)](https://github.com/nvm-sh/nvm)

The project uses nvm to maintain the [Node](https://nodejs.org) version. Compatible Node version is listed in project root [**.nvmrc**](./.nvmrc). Follow the instructions to install nvm for [Linux/macOS](https://github.com/nvm-sh/nvm#installing-and-updating) or [Windows](https://github.com/coreybutler/nvm-windows#installation--upgrades).

Once you install nvm, go to the project directory and switch to the compatible Node version

```console
$ nvm use
```

This will switch to the required Node version if already installed and make `npm` command available in the terminal.

If the required Node version is not installed, it will recommend the command to install it

```console
Found '/Users/default/thirdeye/thirdeye-ui/.nvmrc' with version <14.18.1>
N/A: version "14.18.1 -> N/A" is not yet installed.

You need to run "nvm install 14.18.1" to install it before using it.
```

Following the installation, the command above will let you switch to the required Node version.

> :bulb:<br />`nvm use` (without version number) might not work when using nvm for [Windows](https://github.com/coreybutler/nvm-windows). You may need to specify precise Node version from repository root [**.nvmrc**](/.nvmrc).

#### Configure [Node Package Manager (npm)](https://www.npmjs.com) for use with [Artifactory](https://repo.startreedata.io)

The project may depend on some packages to be installed from Artifactory and npm needs to be configured to allow access to these packages. The Artifactory repository to install packages from is configured in project root [**.npmrc**](./.npmrc).

To configure npm, log in to [Artifactory](https://repo.startreedata.io) and [create an API Key](https://www.jfrog.com/confluence/display/JFROG/User+Profile#UserProfile-APIKey). Then use the API Key to generate npm configuration from Artifactory using `curl`

<!-- prettier-ignore -->
```console
$ curl -u<your-email>:<API-Key> http://repo.startreedata.io/artifactory/api/npm/auth
```

This will generate an authentication token that can be used to configure npm

```console
_auth = <authentication-token>
always-auth = true
email = <your-email>
```

Copy the authentication token and use it in **~/.npmrc**

```npmrc
//repo.startreedata.io/artifactory/api/npm/startree-ui/:_auth=<authentication-token>
```

### Setup

Once you clone the repository, go to the project directory and install

```console
$ npm install
```

This will install necessary dependencies for the project.

> :bulb:<br />In case `npm` errors out with `cb() never called` message, remove project root **/node-modules** and run

```console
$ npm cache clean --force
```

### Run

Once set up, go to the project directory and run

```console
$ npm run start
```

This will build and deploy the project using [webpack-dev-server](https://github.com/webpack/webpack-dev-server) at http://localhost:7004.

> :bulb:<br />Configuration for the proxy to the `/api` endpoint is located in [webpack configuration](./webpack.config.dev.js) under `devServer.proxy` property. Currently,
> it points to the value of `TE_DEV_PROXY_SERVER` environment variable or by default `http://localhost:8080/`.

## Supported browsers

StarTree ThirdEye UI is tested on latest, stable release of [Chrome](https://www.google.com/chrome), [Firefox](https://www.mozilla.org/firefox), [Safari](https://www.apple.com/safari) and [Edge](https://www.microsoft.com/edge).

## Scripts

### `start`

Build and deploy the project using [webpack-dev-server](https://github.com/webpack/webpack-dev-server) at http://localhost:7004

```console
$ npm run start
```

### `build`

Build the project and output the bundles in project root **/dist**

```console
$ npm run build
```

This will also analyze the bundles using [Webpack Bundle Analyzer](https://github.com/webpack-contrib/webpack-bundle-analyzer) and generate bundle report in project root **/webpack**.

### `test`

Run all tests

```console
$ npm run test
```

### `test-watch`

Watch files for changes and re-run tests related to changed files

```console
$ npm run test-watch
```

### `test-coverage`

Run all tests and generate coverage report in project root **/src/test/unit/coverage**

```console
$ npm run test-coverage
```

### `test-e2e`

Run all end to end tests headlessly using [Cypress](https://www.cypress.io)

<!-- prettier-ignore -->
```console
$ npm run test-e2e -- --config baseUrl=<base-URL> --env username=<username>,password=<password>,clientSecret=<client-secret> --browser firefox
```

Following command line arguments are expected

-   [Configuration options](https://docs.cypress.io/guides/references/configuration#Command-Line)
    -   [`baseUrl`](https://docs.cypress.io/guides/references/configuration#Options): server URL to run the tests against
-   [Environment variables](https://docs.cypress.io/guides/guides/environment-variables#Option-4-env)
    -   `username`: username to authenticate with
    -   `password`: password to authenticate with
    -   `clientSecret`: client secret to authenticate with
-   [`browser`](https://docs.cypress.io/guides/guides/command-line#cypress-run-browser-lt-browser-name-or-path-gt): electron (default), chrome, edge or firefox (other than the default Electron browser, any other browser needs to be installed in the environment where tests are being run)

### `test-e2e-gui`

Run all end to end tests using [Cypress Test Runner](https://docs.cypress.io/guides/core-concepts/test-runner)

<!-- prettier-ignore -->
```console
$ npm run test-e2e-gui -- --config baseUrl=<base-URL> --env username=<username>,password=<password>,clientSecret=<client-secret>
```

Following command line arguments are expected

-   [Configuration options](https://docs.cypress.io/guides/references/configuration#Command-Line)
    -   [`baseUrl`](https://docs.cypress.io/guides/references/configuration#Options): server URL to run the tests against
-   [Environment variables](https://docs.cypress.io/guides/guides/environment-variables#Option-4-env)
    -   `username`: username to authenticate with
    -   `password`: password to authenticate with
    -   `clientSecret`: client secret to authenticate with

### `license-header-check`

Run [License Header Checker](https://github.com/georgegillams/license-header-check) across the project except for files and directories listed in project root [**.licenseheaderignore**](./.licenseheaderignore) and check for missing copyright header

```console
$ npm run license-header-check
```

### `license-header-fix`

Run [License Header Checker](https://github.com/georgegillams/license-header-check) across the project except for files and directories listed in project root [**.licenseheaderignore**](./.licenseheaderignore) and insert appropriate copyright header

```console
$ npm run license-header-fix
```

### `eslint-check`

Run [ESLint](https://eslint.org) across the project except for files and directories listed in project root [**.eslintignore**](./.eslintignore) and check for issues

```console
$ npm run eslint-check
```

### `eslint-fix`

Run [ESLint](https://eslint.org) across the project except for files and directories listed in project root [**.eslintignore**](./.eslintignore) and fix issues

```console
$ npm run eslint-fix
```

### `stylelint-check`

Run [stylelint](https://stylelint.io) across the project except for files and directories listed in project root [**.stylelintignore**](./.stylelintignore) and check for issues

```console
$ npm run stylelint-check
```

### `stylelint-fix`

Run [stylelint](https://stylelint.io) across the project except for files and directories listed in project root [**.stylelintignore**](./.stylelintignore) and fix issues

```console
$ npm run stylelint-fix
```

### `prettier-check`

Run [Prettier](https://prettier.io) across the project except for files and directories listed in project root [**.prettierignore**](./.prettierignore) and check for issues

```console
$ npm run prettier-check
```

### `prettier-fix`

Run [Prettier](https://prettier.io) across the project except for files and directories listed in project root [**.prettierignore**](./.prettierignore) and fix issues

```console
$ npm run prettier-fix
```

### `lint-check`

Run [`license-header-check`](#license-header-check), [`eslint-check`](#eslint-check), [`stylelint-check`](#stylelint-check) and [`prettier-check`](#prettier-check) scripts

```console
$ npm run lint-check
```

### `lint-fix`

Run [`license-header-fix`](#license-header-fix), [`eslint-fix`](#eslint-fix), [`stylelint-fix`](#stylelint-fix) and [`prettier-fix`](#prettier-fix) scripts

```console
$ npm run lint-fix
```

## Contributing

[Coding standards, conventions and other things.](./CONTRIBUTING.md)

### `ci-check`

Run all the scripts that will be executed as part of pull request validation for the project

```console
$ npm run ci-check
```
