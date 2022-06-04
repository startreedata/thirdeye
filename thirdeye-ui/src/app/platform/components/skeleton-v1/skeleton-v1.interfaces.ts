// Copyright 2022 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.

import { SkeletonProps } from "@material-ui/lab";

export interface SkeletonV1Props extends SkeletonProps {
    delayInMS?: number;
    preventDelay?: boolean;
}
