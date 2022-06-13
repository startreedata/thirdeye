// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { GridProps } from "@material-ui/core";
import { DimensionV1 } from "./dimension.util";

// Material UI theme property overrides for Grid
export const gridPropsV1: Partial<GridProps> = {
    spacing: DimensionV1.GridSpacingDefault,
};
