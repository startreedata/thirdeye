<img align="right" width="65" height="65" src="./src/public/thirdeye-512x512.png">

# ThirdEye UI Contributing Guidelines

<br/>

The following is a set of guidelines for contributing to the ThirdEye UI project. While some of the guidelines are simple conventions, others are enforced standards.

-   [Coding conventions and standards](#coding-conventions-and-standards)
    -   [License Header Checker](#license-header-checker)
    -   [ESLint](#eslint)
    -   [stylelint](#stylelint)
    -   [Prettier](#prettier)
    -   [Pull request naming standards](#pull-request-naming-standards)
        -   [Commit messages](#commit-messages)

## Coding conventions and standards

Most of the coding standards are enforced with linters.

### [License Header Checker](https://github.com/georgegillams/license-header-check)

Configuration listed in project root [**.license-header.config.json**](./license-header.config.json)

> :bulb:<br />When committing new files, a known issue with this plugin results in copyright header being inserted in files that may not have been staged to be committed.

### [ESLint](https://eslint.org)

Configuration and rules listed in project root [**.eslintrc.yaml**](./.eslintrc.yaml)

### [stylelint](https://stylelint.io)

Configuration and rules listed in project root [**.stylelintrc.yaml**](./.stylelintrc.yaml)

### [Prettier](https://prettier.io)

Configuration and rules listed in project root [**.prettierrc.yaml**](./.prettierrc.yaml)

### Pull request naming standards

Pull request name is tied to how the project is versioned and released. There are checks in place to validate a pull request name but it's possible that a commit with an improper commit message finds its way to the repository. Such a commit might not trigger a project release (if it's expected) or cause improper project version upgrade. Therefore it's important to adhere to the following standards when creating a pull request.

UI pull request naming standards are based on [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0) specification and correlate with [Semantic Versioning](https://semver.org) for the project. A pull request name must be of the form

```
<type>(<scope>)!: <Jira-issue-keys-separated-by-space> <description>
```

#### type

Following types are defined

-   **major**: pull request introducing a major change; correlates with `MAJOR` in Semantic Versioning
    > :bulb:<br />This is a utility type and should only be used under special circumstances.
-   **feat**: pull request introducing a feature; correlates with `MINOR` in Semantic Versioning
-   **fix**: pull request introducing a bug fix; correlates with `PATCH` in Semantic Versioning
-   **wip**: pull request introducing a part of a feature (work in progress); does not trigger a change in version
-   **test**: pull request improving test coverage; does not trigger a change in version
-   **refactor**: pull request improving parts of code; does not trigger a change in version
-   **chore**: pull request introducing miscellaneous changes; does not trigger a change in version

#### scope

Scope defines the project in the repository that the pull request relates to. Following scopes are defined

-   **ui**: pull request introducing a UI project change

#### ! (breaking change)

A pull request introducing a breaking change should use an exclamation mark (`!`) in pull request name. A breaking change correlates with `MAJOR` in Semantic Versioning.

#### Jira issue keys

One or more Jira issue keys can be included, separated by a space.

> :bulb:<br />In case of a bot generated PR, the string `[auto]` should appear here instead of Jira issue keys.

#### description

A short informative summary of the pull request. The only requirements being

-   it does not start with an upper case letter
-   it does not end with a period or ellipsis that GitHub uses to truncate long titles

There is no real character length limit, but keeping it short helps skimming through the commit history.

#### Commit messages

The pull request naming standards apply only to a pull request and not individual commits. When making a commit, you are free to use any commit message except when a pull request consists of a single commit.

In case of a pull request with a single commit, the commit message must adhere to the pull request naming standards. This is because when merging such a pull request, GitHub will suggest using that commit message instead of the pull request name for the merge commit, and it's easy to not notice.

In case you end up creating a pull request with a single commit and a commit message that doesn't adhere to the pull request naming standards, you can update the commit message

```console
$ git commit --amend -m "<new-commit-message>"
$ git push --force-with-lease
```
