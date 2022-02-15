// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Box } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { PageContentsSpacerV1Props } from "./page-contents-spacer-v1.interfaces";

export const PageContentsSpacerV1: FunctionComponent<
    PageContentsSpacerV1Props
> = ({ className, ...otherProps }) => {
    return (
        <Box
            {...otherProps}
            className={classNames(className, "page-contents-spacer-v1")}
            marginBottom={3}
            width="100%"
        />
    );
};
