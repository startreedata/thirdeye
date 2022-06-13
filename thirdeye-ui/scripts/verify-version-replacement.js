#!/usr/bin/env node

const fs = require("fs");
const path = require("path");
const childProcess = require("child_process");

const FILE_CONTAINING_STRING_REPLACEMENT = "dist/thirdeye-ui.js";
const VERSION_STRING_TEMPLATE = "0.0.0-development-thirdeye-ui";
const FILE_CONTAINING_STRING_REPLACEMENT_PATH = path.resolve(
    FILE_CONTAINING_STRING_REPLACEMENT
);

function checkIfTemplateVersionStringExists() {
    const fileContents = fs.readFileSync(
        FILE_CONTAINING_STRING_REPLACEMENT_PATH
    );

    return fileContents.includes(VERSION_STRING_TEMPLATE);
}

function replaceWithLatestTaggedVersion() {
    const latestUIVersionTag = childProcess
        .execSync("git describe --abbrev=0 --tags --match thirdeye-ui\\*")
        .toString()
        .replace("\n", "");

    let fileContents = fs
        .readFileSync(FILE_CONTAINING_STRING_REPLACEMENT_PATH)
        .toString();

    fileContents = fileContents.replace(
        VERSION_STRING_TEMPLATE,
        latestUIVersionTag
    );

    fs.writeFileSync(FILE_CONTAINING_STRING_REPLACEMENT_PATH, fileContents);
}

if (!fs.existsSync(FILE_CONTAINING_STRING_REPLACEMENT_PATH)) {
    console.log(
        "File ",
        FILE_CONTAINING_STRING_REPLACEMENT,
        " does not exist, skipping verification."
    );

    return;
}

if (checkIfTemplateVersionStringExists()) {
    console.log("Replacing version template with latest tag");
    replaceWithLatestTaggedVersion();
} else {
    console.log("Version already replaced");
}
