#!/bin/bash

usage() {
    echo "Usage: startree-ui-projects.pipeline.sh [-t <concourse team>] [-b <git branch>]" 1>&2
}

# Read options
while getopts ":t:b:" opt; do
    case $opt in
        t)
            # Concourse team
            export TEAM=$OPTARG
            ;;
        b)
            # Branch
            export BRANCH=$OPTARG
            ;;
        *)
            # Invalid option
            usage

            return 1
            ;;
    esac
done

if [ -z $TEAM ]; then
    # Set default
    export TEAM="startree-ui"
fi

if [ -z $BRANCH ]; then
    # Set default
    export BRANCH="master"
fi

export PROJECT="thirdeye-ui"
export PIPELINE="$PROJECT-$BRANCH"
export DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null 2>&1 && pwd)"

# Output configuration
echo "Project: $PROJECT"
echo "Concourse team: $TEAM"
echo "Branch: $BRANCH"
echo "Pipeline: $PIPELINE"

# Ask confirmation
read -p "Continue with the configuration? [yN]: " confirmation
case $confirmation in
    y | Y)
        # Set pipeline
        set -x
        fly \
            -t $TEAM \
            set-pipeline \
            --pipeline $PIPELINE \
            --config $DIR/startree-ui-projects.pipeline.yaml \
            --load-vars-from $DIR/$PROJECT.pipeline.yaml \
            --var "branch=$BRANCH" \
            --check-creds
        ;;
    *)
        # Quit
        echo "Abort"
        ;;
esac
