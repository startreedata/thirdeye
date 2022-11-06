#!/bin/bash
#
# Copyright 2022 StarTree Inc
#
# Licensed under the StarTree Community License (the "License"); you may not use
# this file except in compliance with the License. You may obtain a copy of the
# License at http://www.startree.ai/legal/startree-community-license
#
# Unless required by applicable law or agreed to in writing, software distributed under the
# License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
# either express or implied.
#
# See the License for the specific language governing permissions and limitations under
# the License.
#
# Custom script to skim through project static assets and replace any version placeholder with
# latest, released project version number
usage() {
    echo "Usage:" 1>&2
    echo "  release-version-replacement.sh -p <project> -a <assets path>" 1>&2
    echo "Options:" 1>&2
    echo "  -p <project>            thirdeye-ui" 1>&2
    echo "  -a <assets path>        path to project static assets" 1>&2

    exit 1
}

PROJECT=""
ASSETS_PATH=""

# Read options
while getopts ":p:a:" opt; do
    case $opt in
        p)
            # Project
            PROJECT=$OPTARG
            if !(
                    [ $PROJECT == "thirdeye-ui" ]
                ); then
                usage
            fi
            ;;
        a)
            # Assets target
            ASSETS_PATH=$OPTARG
            ;;
        *)
            # Invalid option
            usage
            ;;
    esac
done

if [ -z $PROJECT ]; then
    # Can't proceed
    usage
fi

if [ -z $ASSETS_PATH ]; then
    # Can't proceed
    usage
fi

# Get the latest project release version
PROJECT_VERSION=$(git describe --tags --match "$PROJECT*" --abbrev=0)

if [ -z $PROJECT_VERSION ]; then
    # Can't proceed
    echo "Unable to determine latest project release version"

    exit 1
fi

# Extract project version number
PROJECT_VERSION=${PROJECT_VERSION/$PROJECT-/}

# Remove trailing slash from assets path
ASSETS_PATH=${ASSETS_PATH%/}

# Replace version placeholder in release bundle(s)
VERSION_PLACEHOLDER="0.0.0-development-$PROJECT"
find $ASSETS_PATH -type f -exec perl -i -pe "s/$VERSION_PLACEHOLDER/$PROJECT_VERSION/g" {} +
