#!/bin/sh

main(){
    STATE="$1"
    VERSION="$2"
    
    # Committer Name Detail
    COMMITTER_DETAIL=$(git log -n 1 --pretty=tformat:%an)
    
    # Codebuild Current Build Details
    BUILD_ID=$(echo $CODEBUILD_BUILD_ID | cut -d ":" -f2)
    PROJECT_NAME=$(echo $CODEBUILD_BUILD_ID | cut -d ":" -f1)
    
    # Log location of Current Build Execution Logs
    LOG_LOCATION="https://cb-$BUILD_ENV-codebuild-project-logs.s3.amazonaws.com/$PROJECT_NAME/$BUILD_ID.gz"
    if [ "$STATE" = "START" ]; then
        SUBJECT=$(echo "Build Started for $BUILD_TYPE - $VERSION | $BUILD_ENV | $COMMITTER_DETAIL" | head -c 98)
        MESSAGE="Build has been trigerred on $BUILD_ENV - $BUILD_TYPE - $VERSION by $COMMITTER_DETAIL"
        SNS_PUBLISH "$MESSAGE" "$SUBJECT"
    elif [ "$STATE" = "SUCCESS" ]; then
        SUBJECT=$(echo "Build Completed for $BUILD_TYPE - $VERSION | $BUILD_ENV | $COMMITTER_DETAIL" | head -c 98)
        MESSAGE="Build and Deployment has been Completed Successfully for $BUILD_ENV - $BUILD_TYPE - $VERSION by $COMMITTER_DETAIL. Logs can be downloaded using this link - $LOG_LOCATION. Note: Build Logs link will work over Tunnel or Office Network only"
        SNS_PUBLISH "$MESSAGE" "$SUBJECT"
    elif [ "$STATE" = "FAILED" ]; then
        SUBJECT=$(echo "Build Failed for $BUILD_TYPE - $VERSION | $BUILD_ENV | $COMMITTER_DETAIL" | head -c 98)
        MESSAGE="Build has been Failed on $BUILD_ENV - $BUILD_TYPE - $VERSION by $COMMITTER_DETAIL. Logs can be downloaded using this link - $LOG_LOCATION. Note: Build Logs link will work over Tunnel or Office Network only"
        SNS_PUBLISH "$MESSAGE" "$SUBJECT"
    fi
}

SNS_PUBLISH(){
    MESSAGE=$1
    SUBJECT=$2
    echo "Sending $STATE Mail"
    aws sns publish --topic-arn "$SNS_TOPIC" --message "$MESSAGE" --subject "$SUBJECT"
}

main $*
