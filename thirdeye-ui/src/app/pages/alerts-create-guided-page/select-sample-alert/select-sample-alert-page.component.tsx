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
import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { default as React, FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useOutletContext } from "react-router-dom";
import { SampleAlertSelection } from "../../../components/alert-wizard-v3/sample-alert-selection/sample-alert-selection.component";
import { SampleAlertOption } from "../../../components/alert-wizard-v3/sample-alert-selection/sample-alert-selection.interfaces";
import { generateOptions } from "../../../components/alert-wizard-v3/sample-alert-selection/sample-alert.utils";
import { useAppBarConfigProvider } from "../../../components/app-bar/app-bar-config-provider/app-bar-config-provider.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    PageContentsGridV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { createAlert } from "../../../rest/alerts/alerts.rest";
import { useGetDatasets } from "../../../rest/datasets/datasets.actions";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../../utils/rest/rest.util";
import { getAlertsAlertPath } from "../../../utils/routes/routes.util";
import { QUERY_PARAM_KEY_ANOMALIES_RETRY } from "../../alerts-view-page/alerts-view-page.utils";
import { AlertCreatedGuidedPageOutletContext } from "../alerts-create-guided-page.interfaces";
import { SelectSampleAlertPageProps } from "./select-sample-alert-page.interface";

export const SelectSampleAlertPage: FunctionComponent<SelectSampleAlertPageProps> =
    () => {
        const { t } = useTranslation();
        const navigate = useNavigate();
        const { notify } = useNotificationProviderV1();
        const { setShowAppNavBar } = useAppBarConfigProvider();

        const { datasets, getDatasets, status } = useGetDatasets();

        const { alertTemplates, setShouldShowStepper } =
            useOutletContext<AlertCreatedGuidedPageOutletContext>();

        const [basicAlertSamples, multiDimensionSamples] = useMemo(() => {
            if (datasets === null || alertTemplates === null) {
                return [[], []];
            }

            const sampleAlertOptions = generateOptions(
                datasets,
                alertTemplates
            );

            return [
                sampleAlertOptions.filter(
                    (option) => option.isDimensionExploration === false
                ),
                sampleAlertOptions.filter(
                    (option) => option.isDimensionExploration === true
                ),
            ];
        }, [datasets, alertTemplates]);

        useEffect(() => {
            setShouldShowStepper(false);
            getDatasets();

            // When this page is unloaded show the stepper
            return () => {
                setShouldShowStepper(true);
            };
        }, []);

        const handleSampleAlertSelect = (option: SampleAlertOption): void => {
            const clonedConfiguration = { ...option.alertConfiguration };
            clonedConfiguration.name +=
                "-" + Math.random().toString(36).substring(2, 5);

            createAlert(clonedConfiguration)
                .then((alert) => {
                    navigate(
                        getAlertsAlertPath(
                            alert.id,
                            new URLSearchParams([
                                [QUERY_PARAM_KEY_ANOMALIES_RETRY, "true"],
                            ])
                        )
                    );
                    setShowAppNavBar(true);
                })
                .catch((error: AxiosError): void => {
                    notifyIfErrors(
                        ActionStatus.Error,
                        getErrorMessages(error),
                        notify,
                        t("message.create-error", {
                            entity: t("label.sample-alert"),
                        })
                    );
                });
        };

        return (
            <>
                <PageContentsGridV1>
                    <LoadingErrorStateSwitch
                        wrapInCard
                        wrapInGrid
                        isError={status === ActionStatus.Error}
                        isLoading={
                            status === ActionStatus.Working ||
                            status === ActionStatus.Initial
                        }
                    >
                        <Grid item xs={12}>
                            <SampleAlertSelection
                                basicAlertOptions={basicAlertSamples}
                                multiDimensionAlertOptions={
                                    multiDimensionSamples
                                }
                                onSampleAlertSelect={handleSampleAlertSelect}
                            />
                        </Grid>
                    </LoadingErrorStateSwitch>
                </PageContentsGridV1>

                <WizardBottomBar backBtnLink="../" />
            </>
        );
    };
