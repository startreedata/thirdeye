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

import { Box, Grid, Typography } from "@material-ui/core";
import { capitalize } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { Trans, useTranslation } from "react-i18next";
import { IframeVideoPlayerContainer } from "../../../components/iframe-video-player-container/iframe-video-player-container.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { WelcomeStepCard } from "../../../components/welcome-landing-page/welcome-step-card/welcome-step-card.component";
import {
    LinkV1,
    PageContentsCardV1,
    PageV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetDatasets } from "../../../rest/datasets/datasets.actions";
import {
    getConfigurationPath,
    getDataConfigurationCreatePath,
    getWelcomeCreateAlert,
} from "../../../utils/routes/routes.util";

export const WelcomeLandingPage: FunctionComponent = () => {
    const { t } = useTranslation();

    const { status, datasets, getDatasets } = useGetDatasets();

    const hasDatasets = !!(datasets && datasets.length > 0);

    useEffect(() => {
        getDatasets();
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
                            <Grid item sm={8} xs={12}>
                                <Box clone fontWeight="500" pt={2}>
                                    <Typography variant="body1">
                                        {t("message.welcome-to-thirdeye")}
                                    </Typography>
                                </Box>
                                <Box py={2}>
                                    <Typography color="primary" variant="h4">
                                        {t(
                                            "message.lets-create-your-first-setup"
                                        )}
                                    </Typography>
                                </Box>
                                <Typography variant="body1">
                                    <Trans i18nKey="message.by-creating-an-alert-message" />
                                </Typography>
                            </Grid>
                            <Grid item sm={4} xs={12}>
                                <IframeVideoPlayerContainer>
                                    <iframe
                                        allowFullScreen
                                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                        frameBorder="0"
                                        src={t(
                                            "url.startree-te-demo-video-embed"
                                        )}
                                    />
                                </IframeVideoPlayerContainer>
                            </Grid>
                        </Grid>
                        <Box clone pb={2} pt={4} textAlign="center">
                            <Typography variant="h5">
                                {t("message.complete-the-following-steps")}
                            </Typography>
                        </Box>

                        <LoadingErrorStateSwitch
                            wrapInCard
                            wrapInGrid
                            isError={status === ActionStatus.Error}
                            isLoading={
                                status === ActionStatus.Working ||
                                status === ActionStatus.Initial
                            }
                        >
                            <Box
                                display="flex"
                                flexDirection="row"
                                gridGap={24}
                                justifyContent="center"
                                py={2}
                            >
                                <WelcomeStepCard
                                    ctaContent={t("message.configure-entity", {
                                        entity: t("label.data"),
                                    })}
                                    isComplete={hasDatasets}
                                    link={getDataConfigurationCreatePath()}
                                    subtitle={t(
                                        "message.connect-to-startree-cloud-data-or-add-your-own-pinot-datasource"
                                    )}
                                    title={t(
                                        "message.review-and-configure-data"
                                    )}
                                />
                                <WelcomeStepCard
                                    ctaContent={t("message.create-entity", {
                                        entity: t("label.alert"),
                                    })}
                                    disabled={!hasDatasets}
                                    link={getWelcomeCreateAlert()}
                                    subtitle={t(
                                        "message.explore-startree-thirdeye-in-one-click"
                                    )}
                                    title={capitalize(
                                        t("message.create-my-first-entity", {
                                            entity: t("label.alert"),
                                        })
                                    )}
                                />
                            </Box>
                        </LoadingErrorStateSwitch>

                        <Box clone pb={3} pt={2} textAlign="center">
                            <Typography variant="body2">
                                <Trans i18nKey="message.you-can-always-change-your-setup-in-the-configuration-section">
                                    <LinkV1
                                        color="primary"
                                        href={getConfigurationPath()}
                                        variant="body2"
                                    />
                                </Trans>
                            </Typography>
                        </Box>
                    </Box>
                </PageContentsCardV1>
            </Box>
        </PageV1>
    );
};
