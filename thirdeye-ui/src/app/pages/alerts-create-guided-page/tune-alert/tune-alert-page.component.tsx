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
import { default as React, FunctionComponent, useEffect, useMemo } from "react";
import {
    useNavigate,
    useOutletContext,
    useSearchParams,
} from "react-router-dom";
import { createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { ThresholdSetup } from "../../../components/alert-wizard-v3/threshold-setup/threshold-setup.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../alerts-create-guided-page.interfaces";

export const TuneAlertPage: FunctionComponent = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const {
        alert,
        onAlertPropertyChange,
        selectedAlgorithmOption,
        alertTemplates,
    } = useOutletContext<AlertCreatedGuidedPageOutletContext>();

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

    return (
        <>
            <ThresholdSetup
                alert={alert}
                alertTemplate={selectedAlertTemplate}
                algorithmOptionConfig={selectedAlgorithmOption}
                onAlertPropertyChange={onAlertPropertyChange}
            />

            <WizardBottomBar
                backBtnLink={
                    selectedAlgorithmOption.algorithmOption
                        .alertTemplateForMultidimension === alert.template?.name
                        ? `../${
                              AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION
                          }?${searchParams.toString()}`
                        : `../${
                              AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE
                          }?${searchParams.toString()}`
                }
                nextBtnLink={`../${
                    AppRouteRelative.WELCOME_CREATE_ALERT_ANOMALIES_FILTER
                }?${searchParams.toString()}`}
            />
        </>
    );
};
