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
import { Box, Button, Grid, Typography } from "@material-ui/core";
import DoneAllIcon from "@material-ui/icons/DoneAll";
import { Alert } from "@material-ui/lab";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import ShowMoreText from "react-show-more-text";
import { AlertTypeSectionProps } from "./alert-type-section.interfaces";

export const AlertTypeSection: FunctionComponent<AlertTypeSectionProps> = ({
    option,
    onClick,
    selected,
    recommended,
}) => {
    const { t } = useTranslation();
    const dataTestId = `${option.algorithmOption.title
        .toLowerCase()
        .split(" ")
        .join("-")}-select-btn`;

    return (
        <Grid container>
            {recommended && (
                <Grid item xs={12}>
                    <Alert
                        action={
                            <>
                                <Box display="inline-block" mr={1}>
                                    <DoneAllIcon />
                                </Box>
                                <Box display="inline-block" pr={2}>
                                    {t("label.recommended-type")}
                                </Box>
                            </>
                        }
                        severity="info"
                        variant={selected ? "filled" : "outlined"}
                    >
                        {t("message.this-type-best-first-your-data")}
                    </Alert>
                </Grid>
            )}
            <Grid item xs={12}>
                <Grid
                    container
                    alignItems="center"
                    justifyContent="space-between"
                >
                    <Grid item lg md sm xl xs>
                        <Typography
                            gutterBottom
                            variant="h5"
                            onClick={() => onClick(option.algorithmOption)}
                        >
                            {option.algorithmOption.title}
                        </Typography>
                        <Typography component="p" variant="body2">
                            <ShowMoreText lines={2}>
                                {option.algorithmOption.description}
                            </ShowMoreText>
                        </Typography>
                    </Grid>
                    <Grid item lg={3} md={4} sm={3} xl={2} xs={12}>
                        <Box textAlign="right">
                            <Button
                                color="primary"
                                data-testid={dataTestId}
                                variant="outlined"
                                onClick={() => onClick(option.algorithmOption)}
                            >
                                {selected
                                    ? t("label.selected")
                                    : t("label.select")}
                            </Button>
                        </Box>
                    </Grid>
                </Grid>
            </Grid>

            <Grid item xs={12}>
                <Box>
                    <img
                        src={option.algorithmOption.exampleImage}
                        style={{ width: "100%", height: "auto" }}
                    />
                </Box>
            </Grid>
        </Grid>
    );
};
