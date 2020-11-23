<img align="right" width="65" height="65" src="./src/public/thirdeye-512x512.png">

# ThirdEye UI

<br/>

This is the project for CortexData ThirdEye UI.

-   [Getting Started](#getting-started)
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

##### Node Version Manager [nvm]
The project uses [nvm](https://github.com/nvm-sh/nvm) to maintain the node version.
Once installed, you can switch to the recommended node version using
```
# cd into the thirdeye-ui dir
cd path/to/repo/thirdeye/thirdeye-ui

# Use the node version from .nvmrc
nvm use
``` 
You should have `node` and `npm` available in the terminal.

###### Note
If that version of node isn't installed, nvm will recommend the command to install it.
Following the installation, you can use the same command above to use the recommended node version.

Example:
```
Found '/Users/spyne/repo/thirdeye/thirdeye-ui/.nvmrc' with version <14.7.0>
N/A: version "14.7.0 -> N/A" is not yet installed.

You need to run "nvm install 14.7.0" to install it before using it.
```
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

Run all tests related to changed files (uncommitted files)

```
$ npm run test-watch
```

### `test-coverage`

Run all tests and generate coverage report in project root `src/test/coverage`

```
$ npm run test-coverage
```

### `eslint`

Run ESLint across the project except for files and directories listed in project root `.eslintignore`

```
$ npm run eslint
```

### `stylelint`

Run stylelint across the project except for files and directories listed in project root `.stylelintignore`

```
$ npm run stylelint
```

### `pretty`

Run Prettier across the project except for files and directories listed in project root `.prettierignore`

```
$ npm run pretty
```
