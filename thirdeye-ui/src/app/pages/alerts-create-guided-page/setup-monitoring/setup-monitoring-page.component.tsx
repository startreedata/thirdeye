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
import { default as React, FunctionComponent, useEffect } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { ThresholdSetup } from "../../../components/alert-wizard-v3/threshold-setup/threshold-setup.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../alerts-create-guided-page.interfaces";
import { SetupMonitoringPageProps } from "./setup-monitoring-page.interfaces";

export const SetupMonitoringPage: FunctionComponent<SetupMonitoringPageProps> =
    ({ showEditorSwitchButton }) => {
        const navigate = useNavigate();

        const { alert, onAlertPropertyChange, selectedAlgorithmOption } =
            useOutletContext<AlertCreatedGuidedPageOutletContext>();

        useEffect(() => {
            // On initial render, ensure there is already an alert template selected
            if (!alert.template?.name) {
                navigate(
                    `../${AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE}`
                );
            }
        }, []);

        return (
            <>
                <ThresholdSetup
                    alert={alert}
                    algorithmOptionConfig={selectedAlgorithmOption}
                    showEditorSwitchButton={showEditorSwitchButton}
                    onAlertPropertyChange={onAlertPropertyChange}
                />

                <WizardBottomBar
                    backBtnLink={
                        selectedAlgorithmOption.algorithmOption
                            .alertTemplateForMultidimension ===
                        alert.template?.name
                            ? `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION}`
                            : `../${AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE}`
                    }
                    nextBtnLink={`../${AppRouteRelative.WELCOME_CREATE_ALERT_ANOMALIES_FILTER}`}
                />
            </>
        );
    };
