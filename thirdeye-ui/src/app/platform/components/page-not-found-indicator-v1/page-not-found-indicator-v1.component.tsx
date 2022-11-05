import { Typography } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { PageNotFoundIndicatorV1Props } from "./page-not-found-indicator-v1.inerfaces";
import { usePageNotFoundIndicatorV1Styles } from "./page-not-found-indicator-v1.styles";

export const PageNotFoundIndicatorV1: FunctionComponent<
    PageNotFoundIndicatorV1Props
> = ({ headerText, messageText, className, ...otherProps }) => {
    const pageNotFoundIndicatorV1Classes = usePageNotFoundIndicatorV1Styles();

    return (
        <div
            {...otherProps}
            className={classNames(
                pageNotFoundIndicatorV1Classes.pageNotFoundIndicator,
                className,
                "page-not-found-indicator-v1"
            )}
        >
            <div
                className={
                    pageNotFoundIndicatorV1Classes.pageNotFoundIndicatorContents
                }
            >
                <Typography
                    className={classNames(
                        pageNotFoundIndicatorV1Classes.pageNotFoundIndicatorHeader,
                        "page-not-found-indicator-v1-header"
                    )}
                    color="textSecondary"
                    variant="h2"
                >
                    {headerText}
                </Typography>

                <Typography
                    className="page-not-found-indicator-v1-message"
                    color="textSecondary"
                    variant="h4"
                >
                    {messageText}
                </Typography>
            </div>
        </div>
    );
};
