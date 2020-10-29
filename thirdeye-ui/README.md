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
    -   [`eslint`](#eslint)
    -   [`stylelint`](#stylelint)
    -   [`pretty`](#pretty)

## Getting Started

These instructions will help you get the project up and running on your local machine for development and testing purposes.

### Setup

Once you clone the repository, go to the project directory and install

```
$ npm install
```

This will install necessary dependencies for the peoject.

### Run

Once set up, go to the project directory and run

```
$ npm run start
```

This will build and deploy the project using webpack-dev-server at `http://localhost:7004`.

## Scripts

### `start`

Build and deploy the project using webpack-dev-server at `http://localhost:7004`

```
$ npm run start
```

### `build`

Build the project and output the bundles in project root `dist`

```
$ npm run build
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
