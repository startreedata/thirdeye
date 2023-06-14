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
import { Box, Button, Grid, Link, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { AlertTypeSectionProps } from "./alert-type-section.interfaces";

export const AlertTypeSection: FunctionComponent<AlertTypeSectionProps> = ({
    option,
    onClick,
}) => {
    const { t } = useTranslation();

    return (
        <Grid container>
            <Grid item sm={10} xs={12}>
                <Box>
                    <Link
                        gutterBottom
                        variant="h5"
                        onClick={() => onClick(option.algorithmOption)}
                    >
                        {option.algorithmOption.title}
                    </Link>
                    <Typography component="p" variant="body2">
                        {option.algorithmOption.description}
                    </Typography>
                </Box>

                <Box>
                    <img
                        src={option.algorithmOption.exampleImage}
                        style={{ width: "100%", height: "auto" }}
                    />
                </Box>
            </Grid>

            <Grid item sm={2} xs={12}>
                <Box textAlign="right">
                    <Button
                        color="primary"
                        onClick={() => onClick(option.algorithmOption)}
                    >
                        {t("label.select-type")}
                    </Button>
                </Box>
            </Grid>
        </Grid>
    );
};
