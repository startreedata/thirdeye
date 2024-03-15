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
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext, useSearchParams } from "react-router-dom";
import { InvestigationPreview } from "../../../components/rca/investigation-preview/investigation-preview.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { InvestigationContext } from "../investigation-state-tracker-container-page/investigation-state-tracker.interfaces";
import { TopContributorsSection } from "../../../components/rca/top-contributors-section/top-contributors-section.component";
import { HeatMapSection } from "../../../components/rca/heat-map-section/heat-map-section.component";
import { BaselineOffsetSelection } from "../../../components/rca/analysis-tabs/baseline-offset-selection/baseline-offset-selection.component";
import { PageContentsCardV1 } from "../../../platform/components";
import { useGetAnomalyDimensionAnalysis } from "../../../rest/rca/rca.actions";

export const WhatWherePage: FunctionComponent = () => {
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const context = useOutletContext<InvestigationContext>();

    const anomalyDimensionAnalysisFetch = useGetAnomalyDimensionAnalysis();

    const dimensionsInOrder =
        anomalyDimensionAnalysisFetch?.anomalyDimensionAnalysisData
            ?.dimensions || [];

    const [comparisonOffset, setComparisonOffset] = useState(() => {
        return searchParams.get("baselineWeekOffset") ?? "P1W";
    });

    const handleBaselineChange = (newValue: string): void => {
        setComparisonOffset(newValue);
        searchParams.set("baselineWeekOffset", newValue);
        setSearchParams(searchParams);
    };

    return (
        <>
            <Grid item xs={12}>
                <Typography variant="h4">
                    {t("message.what-went-wrong-and-where")}
                </Typography>
            </Grid>
            <Grid item xs={12}>
                <PageContentsCardV1>
                    <Grid container>
                        <Grid item xs={12}>
                            <Grid
                                container
                                alignItems="center"
                                justifyContent="space-between"
                            >
                                <Grid item>
                                    {t(
                                        "message.select-the-top-contributors-to-see-the-dimensions"
                                    )}
                                </Grid>

                                <Grid item>
                                    <BaselineOffsetSelection
                                        baselineOffset={comparisonOffset}
                                        label={t(
                                            "label.dimensions-changed-from-the-last"
                                        )}
                                        onBaselineOffsetChange={
                                            handleBaselineChange
                                        }
                                    />
                                </Grid>
                            </Grid>
                        </Grid>
                        <Grid item xs={12}>
                            <TopContributorsSection
                                anomalyDimensionAnalysisFetch={
                                    anomalyDimensionAnalysisFetch
                                }
                                comparisonOffset={comparisonOffset}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <HeatMapSection
                                comparisonOffset={comparisonOffset}
                                dimensionsInOrder={dimensionsInOrder}
                            />
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Grid>

            <Grid item xs={12}>
                <InvestigationPreview
                    alertInsight={context.alertInsight}
                    anomaly={context.anomaly}
                    investigation={context.investigation}
                    onInvestigationChange={context.onInvestigationChange}
                />
            </Grid>
            <WizardBottomBar
                nextBtnLink={`../${
                    AppRouteRelative.RCA_EVENTS
                }?${searchParams.toString()}`}
            />
        </>
    );
};
