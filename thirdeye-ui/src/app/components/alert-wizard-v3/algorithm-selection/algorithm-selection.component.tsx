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
import { Box, Button, CardActions, Grid, Typography } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1 } from "../../../platform/components";
import { AlertTemplatesInformationLinks } from "../alert-templates-information-links/alert-templates-information-links";
import { AlgorithmOptionCard } from "./algorithm-option-card/algorithm-option-card.component";
import {
    AlgorithmOption,
    AlgorithmSelectionProps,
} from "./algorithm-selection.interfaces";
import { filterOptionWithTemplateNames } from "./algorithm-selection.utils";

export const AlgorithmSelection: FunctionComponent<AlgorithmSelectionProps> = ({
    onAlertPropertyChange,
    onSelectionComplete,
    simpleOptions,
    advancedOptions,
}) => {
    const { t } = useTranslation();
    const hasNoAvailableAdvancedOptions = useMemo(() => {
        return filterOptionWithTemplateNames(advancedOptions).length === 0;
    }, [advancedOptions]);

    const handleAlgorithmClick = (
        algorithmOption: AlgorithmOption,
        isDimensionExploration: boolean
    ): void => {
        onAlertPropertyChange({
            template: {
                name: isDimensionExploration
                    ? algorithmOption.alertTemplateForMultidimension
                    : algorithmOption.alertTemplate,
            },
        });
        onSelectionComplete(isDimensionExploration);
    };

    return (
        <PageContentsCardV1>
            <Grid container alignItems="stretch">
                <Grid item xs={12}>
                    <Typography variant="h5">
                        {t(
                            "message.select-the-algorithm-type-best-fit-for-your-alert"
                        )}
                    </Typography>
                    <AlertTemplatesInformationLinks />
                </Grid>

                <Grid item xs={12}>
                    <Typography variant="h6">
                        {t("label.range-metric-simple")}
                    </Typography>
                    <Typography variant="body2">
                        {t("message.range-metric-simple-advanced")}
                    </Typography>
                </Grid>

                <Grid item xs={12}>
                    <Grid container spacing={3}>
                        {simpleOptions.map((option) => {
                            return (
                                <Grid
                                    item
                                    key={option.algorithmOption.alertTemplate}
                                    sm={3}
                                    xs={12}
                                >
                                    <AlgorithmOptionCard
                                        option={option.algorithmOption}
                                    >
                                        <CardActions>
                                            <Grid
                                                container
                                                alignItems="center"
                                                justifyContent="space-between"
                                            >
                                                <Grid item md sm={12} xs={12}>
                                                    <Button
                                                        fullWidth
                                                        disabled={
                                                            !option.hasAlertTemplate
                                                        }
                                                        id={`${option.algorithmOption.alertTemplate}-basic-btn`}
                                                        size="small"
                                                        onClick={() =>
                                                            handleAlgorithmClick(
                                                                option.algorithmOption,
                                                                false
                                                            )
                                                        }
                                                    >
                                                        {t("label.basic-alert")}
                                                    </Button>
                                                </Grid>
                                                <Grid item md sm={12} xs={12}>
                                                    <Button
                                                        fullWidth
                                                        disabled={
                                                            !option.hasMultidimension
                                                        }
                                                        id={`${option.algorithmOption.alertTemplate}-dx-btn`}
                                                        size="small"
                                                        onClick={() =>
                                                            handleAlgorithmClick(
                                                                option.algorithmOption,
                                                                true
                                                            )
                                                        }
                                                    >
                                                        {t(
                                                            "label.multi-dimensional-alert"
                                                        )}
                                                    </Button>
                                                </Grid>
                                            </Grid>
                                        </CardActions>
                                    </AlgorithmOptionCard>
                                </Grid>
                            );
                        })}
                    </Grid>
                </Grid>

                <Grid item xs={12}>
                    <Typography variant="h6">
                        {t("label.statistical-forecasting-advanced")}
                    </Typography>
                    <Typography variant="body2">
                        {t(
                            "message.statistical-forecasting-advanced-description"
                        )}
                    </Typography>
                </Grid>

                <Grid item xs={12}>
                    {hasNoAvailableAdvancedOptions && (
                        <Box paddingBottom={2} textAlign="center">
                            <Alert severity="info" variant="outlined">
                                {t(
                                    "message.advanced-options-are-only-available-in-the-enterprise"
                                )}
                            </Alert>
                        </Box>
                    )}
                    <Grid container spacing={3}>
                        {advancedOptions.map((option) => {
                            return (
                                <Grid
                                    item
                                    key={option.algorithmOption.alertTemplate}
                                    sm={3}
                                    xs={12}
                                >
                                    <AlgorithmOptionCard
                                        option={option.algorithmOption}
                                    >
                                        <CardActions>
                                            <Grid
                                                container
                                                alignItems="center"
                                                justifyContent="space-between"
                                            >
                                                <Grid item md sm={12} xs={12}>
                                                    <Button
                                                        fullWidth
                                                        disabled={
                                                            !option.hasAlertTemplate
                                                        }
                                                        size="small"
                                                        onClick={() =>
                                                            handleAlgorithmClick(
                                                                option.algorithmOption,
                                                                false
                                                            )
                                                        }
                                                    >
                                                        {t("label.basic-alert")}
                                                    </Button>
                                                </Grid>
                                                <Grid item md sm={12} xs={12}>
                                                    <Button
                                                        fullWidth
                                                        disabled={
                                                            !option.hasMultidimension
                                                        }
                                                        size="small"
                                                        onClick={() =>
                                                            handleAlgorithmClick(
                                                                option.algorithmOption,
                                                                true
                                                            )
                                                        }
                                                    >
                                                        {t(
                                                            "label.multi-dimensional-alert"
                                                        )}
                                                    </Button>
                                                </Grid>
                                            </Grid>
                                        </CardActions>
                                    </AlgorithmOptionCard>
                                </Grid>
                            );
                        })}
                    </Grid>
                </Grid>
            </Grid>
        </PageContentsCardV1>
    );
};
