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
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
const releaseNotesCommitAnalyzer = require("./scripts/release-notes-commit-analyzer");
const releaseBranch = "master";
const projectName = "thirdeye-ui";
const projectScope = "ui";

module.exports = {
    // Release branches
    branches: [
        {
            name: releaseBranch,
        },
    ],

    // Concourse is not recognized as a CI provider
    ci: false,

    plugins: [
        // Type of next release to be determined based on type and scope of new pull requests
        [
            "@semantic-release/commit-analyzer",
            {
                preset: "conventionalcommits",
                releaseRules: [
                    // Pull request types and scopes that trigger a release for this project
                    {
                        breaking: true,
                        scope: projectScope,
                        release: "major",
                    },
                    {
                        type: "major",
                        scope: projectScope,
                        release: "major",
                    },
                    {
                        type: "feat",
                        scope: projectScope,
                        release: "minor",
                    },
                    {
                        type: "fix",
                        scope: projectScope,
                        release: "patch",
                    },

                    // Pull request scopes that do not belong to this project and will not trigger a release
                    {
                        scope: `!${projectScope}`,
                        release: false,
                    },

                    // Pull request types that do not trigger a release
                    {
                        type: ["chore", "refactor", "test", "wip"],
                        release: false,
                    },
                ],
            },
        ],

        // Release notes to be generated based on type and scope of new commits
        [
            "@semantic-release/release-notes-generator",
            {
                preset: "conventionalcommits",
                writerOpts: {
                    transform: releaseNotesCommitAnalyzer(projectScope),
                },
            },
        ],

        // Add release notes to CHANGELOG.md
        [
            "@semantic-release/changelog",
            {
                changelogFile: "CHANGELOG.md",
            },
        ],

        // Publish package
        "@semantic-release/npm",

        // Create pull request with changelog
        [
            "semantic-release-github-pullrequest",
            {
                assets: ["CHANGELOG.md"],
                baseRef: releaseBranch,
                branch: "changelog-update-${nextRelease.gitTag}",
                pullrequestTitle:
                    "chore(" +
                    projectScope +
                    "): [auto] add changelog for ${nextRelease.gitTag} release",
                labels: ["release"],
            },
        ],

        // Replace placeholder in released source with the version number being released
        [
            "semantic-release-plugin-update-version-in-files",
            {
                files: ["dist/**/*"],
                placeholder: `0.0.0-development-${projectName}`,
            },
        ],

        // Notify on Slack
        [
            "semantic-release-slack-bot",
            {
                slackWebhook: process.env.SLACK_NOTIFICATION_WEBHOOK,
                notifyOnSuccess: true,
                notifyOnFail: false, // CI pipeline will notify upon failure
                onSuccessTemplate: {
                    channel: "#ci-startree-ui-publish",
                    username: `${projectName}-publish-${releaseBranch}`,
                    icon_emoji: ":white_circle:",
                    text:
                        "<!channel> Successfully published `$package_name $npm_package_version`\n" +
                        "Links: <https://repo.startreedata.io/ui/repos/tree/General/startree-ui/$package_name/-/$package_name-$npm_package_version.tgz|Artifactory> | " +
                        "<$repo_url/releases/tag/" +
                        projectName +
                        "-$npm_package_version|Github>",
                },
            },
        ],
    ],

    // GitHub tag for the release
    tagFormat: projectName + "-${version}",
};
