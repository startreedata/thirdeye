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
import {
    Box,
    Button,
    ButtonGroup,
    CardContent,
    Divider,
    Grid,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { PageContentsCardV1 } from "../../../platform/components";
import { AlgorithmOptionCard } from "./algorithm-option-card/algorithm-option-card.component";
import {
    QUERY_PARAM_KEY_FOR_SAMPLE_ALERT_FILTER,
    SampleAlertSelectionProps,
    SAMPLE_ALERT_TYPES,
} from "./sample-alert-selection.interfaces";

export const SampleAlertSelection: FunctionComponent<SampleAlertSelectionProps> =
    ({
        onSampleAlertSelect,
        basicAlertOptions,
        multiDimensionAlertOptions,
    }) => {
        const { t } = useTranslation();
        const [searchParams, setSearchParams] = useSearchParams();

        const filterOption = useMemo(() => {
            return searchParams.get(QUERY_PARAM_KEY_FOR_SAMPLE_ALERT_FILTER);
        }, [searchParams]);

        const handleFilterClick = (newFilter: string | undefined): void => {
            if (newFilter === undefined) {
                searchParams.delete(QUERY_PARAM_KEY_FOR_SAMPLE_ALERT_FILTER);
            } else {
                searchParams.set(
                    QUERY_PARAM_KEY_FOR_SAMPLE_ALERT_FILTER,
                    newFilter
                );
            }

            setSearchParams(searchParams);
        };

        return (
            <PageContentsCardV1>
                <Grid container alignItems="stretch">
                    <Grid item xs={12}>
                        <Grid
                            container
                            alignItems="center"
                            justifyContent="space-between"
                        >
                            <Grid item>
                                <Typography variant="h5">
                                    {t("message.you-can-create-sample-alerts")}
                                </Typography>
                                <Typography variant="body2">
                                    {t(
                                        "message.create-sample-alert-description"
                                    )}
                                </Typography>
                            </Grid>
                            <Grid item>
                                <ButtonGroup color="primary">
                                    <Button
                                        variant="text"
                                        onClick={() =>
                                            handleFilterClick(undefined)
                                        }
                                    >
                                        {t("label.show-all")}
                                    </Button>
                                    <Button
                                        variant="text"
                                        onClick={() =>
                                            handleFilterClick(
                                                SAMPLE_ALERT_TYPES.BASIC
                                            )
                                        }
                                    >
                                        {t("label.basic-alerts-only")}
                                    </Button>
                                    <Button
                                        variant="text"
                                        onClick={() =>
                                            handleFilterClick(
                                                SAMPLE_ALERT_TYPES.MULTIDIMENSION
                                            )
                                        }
                                    >
                                        Multi-Dimension Only
                                    </Button>
                                </ButtonGroup>
                            </Grid>
                        </Grid>
                        <Box padding={1}>
                            <Divider />
                        </Box>
                    </Grid>

                    {(filterOption === SAMPLE_ALERT_TYPES.BASIC ||
                        filterOption === null) && (
                        <Grid item xs={12}>
                            <Grid container>
                                <Grid item xs={12}>
                                    <Typography variant="h6">Basic</Typography>
                                </Grid>
                                {basicAlertOptions.map((option) => {
                                    return (
                                        <Grid
                                            item
                                            key={option.title}
                                            sm={4}
                                            xs={12}
                                        >
                                            <AlgorithmOptionCard
                                                option={option}
                                            >
                                                <CardContent>
                                                    <Button
                                                        color="primary"
                                                        onClick={() =>
                                                            onSampleAlertSelect(
                                                                option
                                                            )
                                                        }
                                                    >
                                                        {t(
                                                            "label.create-sample-alert"
                                                        )}
                                                    </Button>
                                                </CardContent>
                                            </AlgorithmOptionCard>
                                        </Grid>
                                    );
                                })}
                            </Grid>
                        </Grid>
                    )}

                    {(filterOption === SAMPLE_ALERT_TYPES.MULTIDIMENSION ||
                        filterOption === null) && (
                        <Grid item xs={12}>
                            <Grid container>
                                <Grid item xs={12}>
                                    <Typography variant="h6">
                                        Multi-Dimension
                                    </Typography>
                                </Grid>
                                {multiDimensionAlertOptions.map((option) => {
                                    return (
                                        <Grid
                                            item
                                            key={option.title}
                                            sm={4}
                                            xs={12}
                                        >
                                            <AlgorithmOptionCard
                                                option={option}
                                            >
                                                <CardContent>
                                                    <Button
                                                        color="primary"
                                                        onClick={() =>
                                                            onSampleAlertSelect(
                                                                option
                                                            )
                                                        }
                                                    >
                                                        {t(
                                                            "label.create-sample-alert"
                                                        )}
                                                    </Button>
                                                </CardContent>
                                            </AlgorithmOptionCard>
                                        </Grid>
                                    );
                                })}
                            </Grid>
                        </Grid>
                    )}
                </Grid>
            </PageContentsCardV1>
        );
    };
