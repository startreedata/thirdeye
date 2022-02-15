// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Card, CardContent } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { PageContentsCardV1Props } from "./page-contents-card-v1.interfaces";
import { usePageContentsCardV1Styles } from "./page-contents-card-v1.styles";

export const PageContentsCardV1: FunctionComponent<PageContentsCardV1Props> = ({
    fullHeight,
    disablePadding,
    className,
    children,
    ...otherProps
}: PageContentsCardV1Props) => {
    const pageContentsCardV1Classes = usePageContentsCardV1Styles();

    return (
        <Card
            {...otherProps}
            className={classNames(
                pageContentsCardV1Classes.pageContentsCard,
                {
                    [pageContentsCardV1Classes.pageContentsCardFullHeight]: fullHeight,
                    [pageContentsCardV1Classes.pageContentsCardPaddingDisabled]: disablePadding,
                },
                className,
                "page-contents-card-v1"
            )}
        >
            <CardContent
                className={classNames(
                    {
                        [pageContentsCardV1Classes.pageContentsCardFullHeight]: fullHeight,
                        [pageContentsCardV1Classes.pageContentsCardPaddingDisabled]: disablePadding,
                    },
                    "page-contents-card-v1-contents"
                )}
            >
                {children}
            </CardContent>
        </Card>
    );
};
