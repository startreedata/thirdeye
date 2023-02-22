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
import {
    Box,
    Card,
    CardContent,
    Grid,
    IconButton,
    Slider,
    Typography,
} from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { MetadataProperty } from "../../../rest/dto/alert-template.interfaces";
import { AnomaliesFilterPanelProps } from "./anomalies-filter-panel.interfaces";
import { FilterConfigurator } from "./filter-configurator/filter-configurator";

export const AnomaliesFilterPanel: FunctionComponent<AnomaliesFilterPanelProps> =
    ({
        alertTemplate,
        alert,
        onAlertPropertyChange,
        onCloseClick,
        availableConfigurations,
    }) => {
        const { t } = useTranslation();

        const sensitivitySetting = useMemo<MetadataProperty | undefined>(() => {
            if (alertTemplate.properties) {
                return alertTemplate.properties.find(
                    (candidate: MetadataProperty) =>
                        candidate.name === "sensitivity"
                );
            }

            return undefined;
        }, [alertTemplate]);

        return (
            <>
                <Card>
                    <CardContent>
                        <Grid
                            container
                            alignItems="center"
                            justifyContent="space-between"
                        >
                            <Grid item>
                                <Typography variant="h6">
                                    {t("label.filters")}
                                </Typography>
                            </Grid>
                            <Grid item>
                                <IconButton
                                    color="secondary"
                                    size="small"
                                    onClick={onCloseClick}
                                >
                                    <CloseIcon />
                                </IconButton>
                            </Grid>
                        </Grid>
                    </CardContent>
                </Card>
                <Box padding={3}>
                    <Grid container>
                        <Grid item xs={12}>
                            <Typography variant="h4">
                                {t("label.filters-&-sensitivity")}
                            </Typography>
                        </Grid>
                        {sensitivitySetting && (
                            <Grid item xs={12}>
                                <Card variant="elevation">
                                    <CardContent>
                                        {t("label.anomaly-sensitivity")}
                                    </CardContent>
                                    <CardContent>
                                        <Box paddingLeft={3} paddingRight={3}>
                                            <Slider
                                                defaultValue={
                                                    alert.templateProperties
                                                        .sensitivity
                                                        ? Number(
                                                              alert
                                                                  .templateProperties
                                                                  .sensitivity
                                                          )
                                                        : sensitivitySetting.defaultValue
                                                        ? Number(
                                                              sensitivitySetting.defaultValue
                                                          )
                                                        : 6
                                                }
                                                marks={[
                                                    {
                                                        value: -14,
                                                        label: t("label.low"),
                                                    },
                                                    {
                                                        value: 6,
                                                        label: t(
                                                            "label.medium"
                                                        ),
                                                    },
                                                    {
                                                        value: 26,
                                                        label: t("label.high"),
                                                    },
                                                ]}
                                                max={26}
                                                min={-14}
                                                step={1}
                                                onChange={(_, value) => {
                                                    onAlertPropertyChange({
                                                        templateProperties: {
                                                            ...alert.templateProperties,
                                                            sensitivity:
                                                                value as number,
                                                        },
                                                    });
                                                }}
                                            />
                                        </Box>
                                    </CardContent>
                                </Card>
                            </Grid>
                        )}

                        {availableConfigurations.map((item) => {
                            return (
                                <Grid item key={item.name} xs={12}>
                                    <FilterConfigurator
                                        alert={alert}
                                        renderConfig={item}
                                        onAlertPropertyChange={
                                            onAlertPropertyChange
                                        }
                                    />
                                </Grid>
                            );
                        })}
                    </Grid>
                </Box>
            </>
        );
    };
