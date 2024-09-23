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
import { Alert, AlertTitle } from "@material-ui/lab";
import { default as React, FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import {
    useNavigate,
    useOutletContext,
    useSearchParams,
} from "react-router-dom";
import { createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { NavigateAlertCreationFlowsDropdown } from "../../../components/alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown";
import { ThresholdSetup } from "../../../components/alert-wizard-v3/threshold-setup/threshold-setup-v2.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { PageContentsGridV1 } from "../../../platform/components";
import { ColorV1 } from "../../../platform/utils/material-ui/color.util";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../alerts-create-guided-page.interfaces";

export const TuneAlertPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const {
        alert,
        onAlertPropertyChange,
        selectedAlgorithmOption,
        alertTemplates,

        alertRecommendations,
    } = useOutletContext<AlertCreatedGuidedPageOutletContext>();

    const recommendedAlertConfigMatchingTemplate = useMemo(() => {
        if (alertRecommendations && alert.template?.name) {
            return alertRecommendations.find(
                (candidate) =>
                    candidate.alert.template?.name === alert.template?.name
            );
        }

        return undefined;
    }, [alertRecommendations, alert]);

    const doesAlertHaveRecommendedValues = useMemo(() => {
        let hasValues = true;

        if (!recommendedAlertConfigMatchingTemplate) {
            return false;
        }

        Object.keys(
            recommendedAlertConfigMatchingTemplate.alert.templateProperties
        ).forEach((k) => {
            hasValues =
                hasValues &&
                recommendedAlertConfigMatchingTemplate.alert.templateProperties[
                    k
                ] === alert.templateProperties[k];
        });

        return hasValues;
    }, [recommendedAlertConfigMatchingTemplate, alert]);

    useEffect(() => {
        // On initial render, ensure there is already an alert template selected
        if (!alert.template?.name) {
            navigate(`../${AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE}`);
        }

        // On initial render, ensure metric is selected
        const newAlert = createNewStartingAlert();

        const metricIsSelected =
            newAlert.templateProperties.dataset !==
                alert.templateProperties.dataset &&
            newAlert.templateProperties.aggregationColumn !==
                alert.templateProperties.aggregationColumn;

        if (!metricIsSelected) {
            navigate(
                `../${AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_METRIC}`
            );
        }
    }, []);

    const selectedAlertTemplate = useMemo(() => {
        return alertTemplates.find((alertTemplateCandidate) => {
            return alertTemplateCandidate.name === alert.template?.name;
        });
    }, [alertTemplates, alert]);

    const handleTuneAlertClick = (): void => {
        if (!recommendedAlertConfigMatchingTemplate) {
            return;
        }
        onAlertPropertyChange({
            templateProperties: {
                ...alert.templateProperties,
                ...recommendedAlertConfigMatchingTemplate.alert
                    .templateProperties,
            },
        });
    };

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <Grid
                        container
                        alignItems="center"
                        justifyContent="space-between"
                    >
                        <Grid item>
                            <Typography variant="h5">
                                {t("label.alert-setup")}
                            </Typography>
                            <Typography variant="body1">
                                {t("message.alert-setup-description")}
                            </Typography>
                        </Grid>
                        <Grid item>
                            <NavigateAlertCreationFlowsDropdown />
                        </Grid>
                    </Grid>
                </Grid>
                {recommendedAlertConfigMatchingTemplate && (
                    <Grid item xs={12}>
                        <Alert
                            action={
                                <>
                                    {doesAlertHaveRecommendedValues ? (
                                        <Box
                                            alignContent="center"
                                            position="flex"
                                            style={{ color: ColorV1.Green2 }}
                                            textAlign="center"
                                        >
                                            <Box mr={1}>
                                                <DoneAllIcon />
                                            </Box>
                                            <Box pr={2}>Alert Tuned</Box>
                                        </Box>
                                    ) : (
                                        <Button
                                            color="primary"
                                            onClick={handleTuneAlertClick}
                                        >
                                            {t("label.tune-my-alert")}
                                        </Button>
                                    )}
                                </>
                            }
                            severity="info"
                            style={{ backgroundColor: "#FFF" }}
                            variant="outlined"
                        >
                            <AlertTitle>
                                {t("message.we-can-tune-the-alert-for-you")}
                            </AlertTitle>
                            {t(
                                "message.our-new-feature-sets-up-your-alert-with-the-parameters"
                            )}
                        </Alert>
                    </Grid>
                )}

                <Grid item xs={12}>
                    <ThresholdSetup
                        alert={alert}
                        alertTemplate={selectedAlertTemplate}
                        algorithmOptionConfig={selectedAlgorithmOption}
                        onAlertPropertyChange={onAlertPropertyChange}
                    />
                </Grid>
            </PageContentsGridV1>
            <WizardBottomBar
                backBtnLink={`../${
                    AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE
                }?${searchParams.toString()}`}
                nextBtnLink={`../${
                    AppRouteRelative.WELCOME_CREATE_ALERT_ANOMALIES_FILTER
                }?${searchParams.toString()}`}
            />
        </>
    );
};
