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
import { Box, Divider, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext } from "react-router-dom";
import { AlertJson } from "../../components/alert-wizard-v2/alert-json/alert-json.component";
import { AlertNotifications } from "../../components/alert-wizard-v2/alert-notifications/alert-notifications.component";
import { PreviewChart } from "../../components/alert-wizard-v2/alert-template/preview-chart/preview-chart.component";
import { Portal } from "../../components/portal/portal.component";
import { WizardBottomBar } from "../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../platform/components";
import {
    AlertsSimpleAdvancedJsonContainerPageOutletContextProps,
    BOTTOM_BAR_ELEMENT_ID,
} from "../alerts-edit-create-common/alerts-edit-create-common-page.interfaces";

export const AlertsUpdateJSONPage: FunctionComponent = () => {
    const { t } = useTranslation();

    const [submitBtnLabel, setSubmitBtnLabel] = useState<string>(
        t("label.update-entity", {
            entity: t("label.alert"),
        })
    );
    const [isSubmitBtnEnabled, setIsSubmitBtnEnabled] = useState(false);
    const [isAlertValid, setIsAlertValid] = useState(true);
    const {
        alert,
        handleAlertPropertyChange: onAlertPropertyChange,
        selectedSubscriptionGroups,
        handleSubscriptionGroupChange: onSubscriptionGroupsChange,
        isEditRequestInFlight,
        handleSubmitAlertClick,
        onPageExit,
    } = useOutletContext<AlertsSimpleAdvancedJsonContainerPageOutletContextProps>();

    useEffect(() => {
        setIsSubmitBtnEnabled(false);
        setSubmitBtnLabel(
            t("message.preview-alert-in-chart-before-submitting")
        );
    }, []);

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <AlertJson
                            alert={alert}
                            setIsAlertValid={setIsAlertValid}
                            onAlertPropertyChange={onAlertPropertyChange}
                        />
                        <Box marginBottom={3} marginTop={3}>
                            <Divider />
                        </Box>
                        <Box>
                            <PreviewChart
                                alert={alert}
                                disableReload={!isAlertValid}
                                onAlertPropertyChange={onAlertPropertyChange}
                                onChartDataLoadSuccess={() => {
                                    setIsSubmitBtnEnabled(true);
                                    setSubmitBtnLabel(
                                        t("label.update-entity", {
                                            entity: t("label.alert"),
                                        })
                                    );
                                }}
                            />
                        </Box>
                    </PageContentsCardV1>
                </Grid>
                <Grid item xs={12}>
                    <AlertNotifications
                        alert={alert}
                        initiallySelectedSubscriptionGroups={
                            selectedSubscriptionGroups
                        }
                        onSubscriptionGroupsChange={onSubscriptionGroupsChange}
                    />
                </Grid>
            </PageContentsGridV1>

            <Portal containerId={BOTTOM_BAR_ELEMENT_ID}>
                <WizardBottomBar
                    doNotWrapInContainer
                    backButtonLabel={t("label.cancel")}
                    handleBackClick={onPageExit}
                    handleNextClick={() => handleSubmitAlertClick(alert)}
                    nextButtonIsDisabled={
                        !isSubmitBtnEnabled ||
                        isEditRequestInFlight ||
                        !isAlertValid
                    }
                    nextButtonLabel={submitBtnLabel}
                />
            </Portal>
        </>
    );
};
