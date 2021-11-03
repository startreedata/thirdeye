#!/bin/bash

usage() {
    echo "Usage: startree-ui-projects-test-e2e.pipeline.sh -p <auth-ui|platform-ui|data-manager-ui|mission-control-ui|my-apps-ui> [-t <concourse team>] [-b <git branch>] [-u <baseUrl>]" 1>&2
}

# Read options
while getopts ":p:t:b:u:" opt; do
    case $opt in
        p)
            # Project
            export PROJECT=$OPTARG
            if !([ $PROJECT == "auth-ui" ] || [ $PROJECT == "platform-ui" ] || [ $PROJECT == "data-manager-ui" ] || [ $PROJECT == "mission-control-ui" ] || [ $PROJECT == "my-apps-ui" ]); then
                usage

                return 1
            fi
            ;;
        t)
            # Concourse team
            export TEAM=$OPTARG
            ;;
        b)
            # Branch
            export BRANCH=$OPTARG
            ;;
        u)
            # Base URL
            export BASE_URL=$OPTARG
            ;;
        :)
            echo "Option -$OPTARG requires an argument." >&2

            return 1
            ;;
        *)
            # Invalid option
            usage

            return 1
            ;;
    esac
done

if [ -z $PROJECT ]; then
    # Can't proceed
    usage

    return 1
fi

if [ -z $BASE_URL ]; then
    # Can't proceed
    usage

    return 1
fi

if [ -z $TEAM ]; then
    # Set default
    export TEAM="startree-ui"
fi

if [ -z $BRANCH ]; then
    # Set default
    export BRANCH="master"
fi

export PIPELINE="$PROJECT-$BRANCH-test-e2e"
export DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null 2>&1 && pwd)"

# Output configuration
echo "Project: $PROJECT"
echo "Concourse team: $TEAM"
echo "Branch: $BRANCH"
echo "Pipeline: $PIPELINE"
echo "Base URL: $BASE_URL"

# Ask confirmation
read -p "Continue with the configuration? [yN]: " confirmation
case $confirmation in
    y | Y)
        # Set pipeline
        fly \
            -t $TEAM \
            set-pipeline \
            --pipeline $PIPELINE \
            --config $DIR/startree-ui-test-e2e.pipeline.yaml \
            --load-vars-from $DIR/$PROJECT.pipeline.yaml \
            --var "branch=$BRANCH" \
            --var "baseUrl=$BASE_URL" \
            --check-creds
        ;;
    *)
        # Quit
        echo "Aborted"
        ;;
esac
