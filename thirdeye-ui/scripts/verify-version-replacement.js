#!/usr/bin/env node
/* eslint-disable */
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
const fs = require("fs");
const path = require("path");
const childProcess = require("child_process");

const THIRDEYE_UI_JS_FILE_PATTERN = /thirdeye-ui\.\w*\.js/;
const VERSION_STRING_TEMPLATE = "0.0.0-development-thirdeye-ui";

function checkIfTemplateVersionStringExists(thirdEyeUiFile) {
    const fileContents = fs.readFileSync(thirdEyeUiFile);

    return fileContents.includes(VERSION_STRING_TEMPLATE);
}

function replaceWithLatestTaggedVersion(thirdEyeUiFile) {
    const latestUIVersionTag = childProcess
        .execSync("git describe --abbrev=0 --tags --match thirdeye-ui\\*")
        .toString()
        .replace("\n", "");

    let fileContents = fs.readFileSync(thirdEyeUiFile).toString();

    fileContents = fileContents.replace(
        VERSION_STRING_TEMPLATE,
        latestUIVersionTag
    );

    fs.writeFileSync(thirdEyeUiFile, fileContents);
}

const distFiles = fs.readdirSync(path.resolve("dist"));
const thirdEyeUiFile = distFiles.find((name) =>
    THIRDEYE_UI_JS_FILE_PATTERN.test(name)
);

if (!thirdEyeUiFile) {
    console.log(
        "File ",
        FILENAME_START_CONTAINING_STRING_REPLACEMENT,
        " does not exist, skipping verification."
    );

    return;
}

if (checkIfTemplateVersionStringExists(path.join("dist", thirdEyeUiFile))) {
    console.log("Replacing version template with latest tag");
    replaceWithLatestTaggedVersion(path.join("dist", thirdEyeUiFile));
} else {
    console.log("Version already replaced");
}
