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

import { Box, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { WelcomeStepCard } from "../../components/welcome-landing-page/welcome-step-card/welcome-step-card.component";
import { PageContentsCardV1, PageV1 } from "../../platform/components";
import { DimensionV1 } from "../../platform/utils";
import { ActionStatus } from "../../rest/actions.interfaces";
import { getAllDatasets } from "../../rest/datasets/datasets.rest";
import { getDataConfigurationCreatePath } from "../../utils/routes/routes.util";

export const WelcomeLandingPage: FunctionComponent = () => {
    const [status, setStatus] = useState<
        typeof ActionStatus[keyof typeof ActionStatus]
    >(ActionStatus.Initial);
    const [hasDatasets, setHasDatasets] = useState<boolean>(false);
    const navigate = useNavigate();

    useEffect(() => {
        setStatus(ActionStatus.Working);
        getAllDatasets()
            .then((datasets) => {
                if (datasets && datasets.length > 0) {
                    setHasDatasets(true);
                }
                setStatus(ActionStatus.Done);
            })
            .catch(() => {
                setStatus(ActionStatus.Error);
            });
    }, []);

    return (
        <PageV1>
            <Box display="flex" flexDirection="column" p={7} width="100%">
                <PageContentsCardV1 fullHeight>
                    <Box pt={4} px={8}>
                        <Grid
                            container
                            alignItems="center"
                            direction="row"
                            justifyContent="center"
                            spacing={4}
                        >
                            <Grid item xs={8}>
                                <Box clone fontWeight="500" pt={2}>
                                    <Typography variant="body1">
                                        Welcome to ThirdEye
                                    </Typography>
                                </Box>
                                <Box py={2}>
                                    <Typography color="primary" variant="h4">
                                        Let&apos;s create your first setup
                                    </Typography>
                                </Box>
                                <Typography variant="body1">
                                    By creating an <strong>Alert</strong>,
                                    you&apos;ll be able to&nbsp;
                                    <strong>Monitor your KPIs</strong> and&nbsp;
                                    <strong>Anomalies</strong> will help you to
                                    find&nbsp;
                                    <strong>Outliers in the KPIs</strong>&nbsp;
                                    and analyze the Root Cause Analysis.
                                </Typography>
                            </Grid>
                            <Grid item xs={4}>
                                <Box
                                    border="1px solid"
                                    borderColor="primary.main"
                                    borderRadius={
                                        DimensionV1.BorderRadiusDefault
                                    }
                                    style={{
                                        aspectRatio: "16 / 9",
                                    }}
                                    textAlign="center"
                                    width="100%"
                                >
                                    {/* TODO: Demo video goes here */}
                                </Box>
                            </Grid>
                        </Grid>
                        <Box clone pb={2} pt={4} textAlign="center">
                            <Typography variant="h5">
                                Complete the following steps
                            </Typography>
                        </Box>

                        <LoadingErrorStateSwitch
                            isError={status === ActionStatus.Error}
                            isLoading={status === ActionStatus.Working}
                        >
                            <Box
                                display="flex"
                                flexDirection="row"
                                gridGap={24}
                                justifyContent="center"
                                py={2}
                            >
                                <WelcomeStepCard
                                    ctaContent="Configure data"
                                    isComplete={hasDatasets}
                                    subtitle="Connect to StarTree cloud data or add your own Pinot datasource"
                                    title="Review and configure data"
                                    onClickCta={() => {
                                        navigate(
                                            getDataConfigurationCreatePath()
                                        );
                                    }}
                                />
                                <WelcomeStepCard
                                    ctaContent="Create alert"
                                    disabled={!hasDatasets}
                                    subtitle="Explore StarTree ThirdEye in one click"
                                    title="Create my first alert"
                                />
                            </Box>
                        </LoadingErrorStateSwitch>

                        <Box clone pb={3} pt={2} textAlign="center">
                            <Typography color="secondary" variant="body2">
                                You can always change your setup in the
                                configuration section.
                            </Typography>
                        </Box>
                    </Box>
                </PageContentsCardV1>
            </Box>
        </PageV1>
    );
};
