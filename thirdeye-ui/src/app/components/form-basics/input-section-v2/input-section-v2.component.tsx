/*
 * Copyright 2023 StarTree Inc
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
import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { InputSectionV2Props } from "./input-section-v2.interfaces";

export const InputSectionV2: FunctionComponent<InputSectionV2Props> = ({
    label,
    helperLabel,
    labelComponent,
    inputComponent,
    gridContainerProps,
    description,
    isOptional,
}) => {
    const { t } = useTranslation();

    return (
        <Grid item xs={12}>
            <Grid container alignItems="center" {...gridContainerProps}>
                <Grid item xs={12}>
                    {!!labelComponent && labelComponent}
                    {!labelComponent && label && (
                        <>
                            <Typography variant="body2">
                                {label}
                                {isOptional && (
                                    <Typography variant="caption">
                                        {"  "}({t("label.optional")})
                                    </Typography>
                                )}{" "}
                            </Typography>
                            {helperLabel && (
                                <Typography variant="caption">
                                    {helperLabel}
                                </Typography>
                            )}
                        </>
                    )}
                    {inputComponent}
                    {description && (
                        <>
                            <Typography variant="caption">
                                {description}
                            </Typography>
                        </>
                    )}
                </Grid>
            </Grid>
        </Grid>
    );
};
