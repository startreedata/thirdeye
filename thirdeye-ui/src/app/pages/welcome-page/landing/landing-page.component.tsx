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

import { Box, Link, Typography } from "@material-ui/core";
import { capitalize } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { WelcomeStepCard } from "../../../components/welcome-landing-page/welcome-step-card/welcome-step-card.component";
import { WELCOME_STEP_TEST_IDS } from "../../../components/welcome-landing-page/welcome-step-card/welcome-step-card.interfaces";
import {
    // LinkV1,
    PageContentsCardV1,
    PageV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetDatasets } from "../../../rest/datasets/datasets.actions";
import {
    getDataConfigurationCreatePath,
    // getHomePath,
    getAlertsEasyCreatePath,
} from "../../../utils/routes/routes.util";
import { useGetAlertsCount } from "../../../rest/alerts/alerts.actions";
// import { useNavigate } from "react-router-dom";
// import {
// useAppBarConfigProvider
// } from "../../../components/app-bar/app-bar-config-provider/app-bar-config-provider.component";
import { useGetAlertTemplates } from "../../../rest/alert-templates/alert-templates.actions";

import { useCheckLoadedTemplates } from "../../../hooks/useCheckLoadedTemplates";
import { QUERY_PARAM_KEYS } from "../../../utils/constants/constants.util";

export const WelcomeLandingPage: FunctionComponent = () => {
    const { t } = useTranslation();
    // const navigate = useNavigate();
    // const { setShowAppNavBar } = useAppBarConfigProvider();

    const { status, datasets, getDatasets } = useGetDatasets();
    const { alertsCount, getAlertsCount } = useGetAlertsCount();
    const {
        alertTemplates,
        getAlertTemplates,
        status: alertTemplatesRequestStatus,
    } = useGetAlertTemplates();

    const hasDatasets = !!(datasets && datasets.length > 0);

    useEffect(() => {
        getDatasets();
        getAlertsCount();
        getAlertTemplates();
    }, []);

    useEffect(() => {
        if (alertsCount?.count) {
            // setShowAppNavBar(true);
            // navigate(getHomePath());

            return;
        }
    }, [alertsCount?.count]);

    useCheckLoadedTemplates({ alertTemplates, alertTemplatesRequestStatus });

    return (
        <PageV1>
            <Box display="flex" flexDirection="column" p={7} width="100%">
                <PageContentsCardV1 fullHeight>
                    <Box
                        alignItems="center"
                        display="flex"
                        flexDirection="column"
                        pt={4}
                        px={8}
                    >
                        <Typography variant="h3">
                            {t("message.welcome-to-thirdeye")}
                        </Typography>
                        <Typography variant="body1">
                            {t("message.start-monitoring-your-data")}
                        </Typography>
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
                                    btnTestId={
                                        WELCOME_STEP_TEST_IDS.CONFIGURE_BUTTON
                                    }
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
                                    btnTestId={
                                        WELCOME_STEP_TEST_IDS.CREATE_ALERT
                                    }
                                    ctaContent={t("message.create-entity", {
                                        entity: t("label.alert"),
                                    })}
                                    disabled={!hasDatasets}
                                    link={getAlertsEasyCreatePath(
                                        new URLSearchParams([
                                            [
                                                QUERY_PARAM_KEYS.IS_FIRST_ALERT,
                                                "true",
                                            ],
                                        ])
                                    )}
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
                                {t("message.need-help")}
                                <Link
                                    color="primary"
                                    href="https://www.youtube.com/playlist?list=PLihIrF0tCXdc7xMW6549RO23CxgvRkZQO"
                                    target="_blank"
                                    underline="always"
                                >
                                    {" "}
                                    here
                                </Link>
                            </Typography>
                        </Box>
                    </Box>
                </PageContentsCardV1>
            </Box>
        </PageV1>
    );
};
