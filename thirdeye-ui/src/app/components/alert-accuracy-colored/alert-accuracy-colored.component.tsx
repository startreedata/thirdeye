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

import { Typography, useTheme } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { SkeletonV1 } from "../../platform/components";
import { getAlertAccuracyData } from "../../utils/alerts/alerts.util";
import type { AlertAccuracyColoredProps } from "./alert-accuracy-colored.interface";

export const AlertAccuracyColored: FunctionComponent<AlertAccuracyColoredProps> =
    ({
        alertStats,
        renderCustomLoading,
        typographyProps,
        renderCustomText,
        defaultSkeletonProps,
    }) => {
        const theme = useTheme();
        const { t } = useTranslation();

        if (!alertStats) {
            return (
                renderCustomLoading || (
                    <SkeletonV1 width={50} {...defaultSkeletonProps} />
                )
            );
        }

        const [accuracyNumber, colorScheme] = getAlertAccuracyData(alertStats);

        const accuracyString = `${t("label.accuracy")}: ${(
            100 * accuracyNumber
        ).toFixed(2)}%`;
        const color = theme.palette[colorScheme].main;

        return (
            <Typography style={{ color }} variant="body1" {...typographyProps}>
                {renderCustomText?.(accuracyNumber) || accuracyString}
            </Typography>
        );
    };
