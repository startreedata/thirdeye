/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

import { Typography, TypographyProps, useTheme } from "@material-ui/core";
import React, { FunctionComponent, ReactElement } from "react";
import { SkeletonV1 } from "../../platform/components";
import { SkeletonV1Props } from "../../platform/components/skeleton-v1/skeleton-v1.interfaces";
import { AlertStats } from "../../rest/dto/alert.interfaces";
import { getAlertAccuracyData } from "../../utils/alerts/alerts.util";

export const AlertAccuracyColored: FunctionComponent<{
    alertStats: AlertStats | null;
    renderCustomLoading?: ReactElement;
    defaultSkeletonProps?: SkeletonV1Props;
    typographyProps?: Partial<TypographyProps>;
    renderCustomText?: (accuracy: number) => ReactElement;
}> = ({
    alertStats,
    renderCustomLoading,
    typographyProps,
    renderCustomText,
    defaultSkeletonProps,
}) => {
    const theme = useTheme();

    if (!alertStats) {
        return (
            renderCustomLoading || (
                <SkeletonV1 width={50} {...defaultSkeletonProps} />
            )
        );
    }

    const [accuracyNumber, colorScheme] = getAlertAccuracyData(alertStats);

    const accuracyString = `Accuracy: ${100 * accuracyNumber}%`;
    const color = theme.palette[colorScheme].main;

    return (
        <Typography style={{ color }} variant="body1" {...typographyProps}>
            {renderCustomText?.(accuracyNumber) || accuracyString}
        </Typography>
    );
};
