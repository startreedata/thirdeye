// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { LinkProps } from "@material-ui/core";

// Material UI theme style overrides for Link
export const linkClassesV1 = {
    root: {
        cursor: "pointer",
    },
};

// Material UI theme property overrides for Link
export const linkPropsV1: Partial<LinkProps> = {
    underline: "none",
    rel: "noopener",
};
