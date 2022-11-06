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
// Custom transform function to analyze release commits and generate release notes
module.exports = (projectScope) => {
    return (commit) => {
        if (commit.scope !== projectScope) {
            // Commit scopes that do not belong to the given project need not appear in release
            // notes
            return;
        }

        // Commit scope need not appear in release notes
        commit.scope = "";

        // Commit types that appear under appropriate section in release notes
        if (commit.type === "major") {
            commit.type = "Major";
        } else if (commit.type === "feat") {
            commit.type = "Features";
        } else if (commit.type === "fix") {
            commit.type = "Bug Fixes";
        } else {
            // Commit types that need not appear in release notes
            return;
        }

        // Generate readable, partial commit hash
        if (commit.commit && commit.commit.short) {
            commit.shortHash = commit.commit.short;
        } else if (typeof commit.hash === "string") {
            commit.shortHash = commit.hash.substring(0, 8);
        }

        // Eliminate pull request number/reference
        if (typeof commit.subject === "string") {
            commit.subject = commit.subject.replace(/[ ]*\(#[0-9]+\)$/g, "");
        }
        commit.references = [];

        // Insert issue reference
        if (typeof commit.subject === "string") {
            const issues = commit.subject.match(/[A-Z]+-[0-9]+/g);
            issues &&
                issues.forEach((issue) => {
                    commit.subject = commit.subject.replace(
                        issue,
                        `[${issue}](https://cortexdata.atlassian.net/browse/${issue})`
                    );
                });
        }

        return commit;
    };
};
