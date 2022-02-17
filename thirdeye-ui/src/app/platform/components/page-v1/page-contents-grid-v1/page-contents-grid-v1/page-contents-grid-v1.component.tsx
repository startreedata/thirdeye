// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Grid } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { DimensionV1 } from "../../../../utils/material-ui/dimension.util";
import { PageContentsGridV1Props } from "./page-contents-grid-v1.interfaces";
import { usePageContentsGridV1Styles } from "./page-contents-grid-v1.styles";

export const PageContentsGridV1: FunctionComponent<PageContentsGridV1Props> = ({
    fullHeight,
    className,
    children,
    ...otherProps
}) => {
    const pageContentsGridV1Classes = usePageContentsGridV1Styles();

    return (
        <div
            {...otherProps}
            className={classNames(
                pageContentsGridV1Classes.pageContentsGrid,
                {
                    [pageContentsGridV1Classes.pageContentsGridFullHeight]:
                        fullHeight,
                },
                className,
                "page-contents-grid-v1"
            )}
        >
            <Grid
                container
                className={classNames(
                    {
                        [pageContentsGridV1Classes.pageContentsGridFullHeight]:
                            fullHeight,
                    },
                    "page-contents-grid-v1-grid"
                )}
                spacing={DimensionV1.PageGridSpacing}
            >
                {children}
            </Grid>
        </div>
    );
};
