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

import { Box, Button, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { DimensionV1 } from "../../../platform/utils";
import type { WelcomeStepCardProps } from "./welcome-step-card.interfaces";

export const WelcomeStepCard: FunctionComponent<WelcomeStepCardProps> = ({
    title,
    subtitle,
    ctaText,
    disabled = false,
    onClickCta = () => null,
}) => {
    return (
        <Box
            alignItems="center"
            border="1px solid"
            borderColor="secondary.light"
            borderRadius={DimensionV1.BorderRadiusDefault}
            display="flex"
            flexDirection="column"
            pb={2}
            pt={4}
            px={4}
            style={{
                // TODO: Move to styles
                opacity: disabled ? 0.6 : 1,
            }}
            textAlign="center"
            width={400}
        >
            <Box clone fontWeight="bold">
                <Typography
                    color={disabled ? "initial" : "primary"}
                    variant="subtitle1"
                >
                    {title}
                </Typography>
            </Box>
            <Box clone m="auto" py={2}>
                <Typography variant="body2">{subtitle}</Typography>
            </Box>
            <Box clone width="80%">
                <Button
                    color={disabled ? "default" : "primary"}
                    disabled={disabled}
                    variant={disabled ? "outlined" : "contained"}
                    onClick={onClickCta}
                >
                    {ctaText}
                </Button>
            </Box>
        </Box>
    );
};
