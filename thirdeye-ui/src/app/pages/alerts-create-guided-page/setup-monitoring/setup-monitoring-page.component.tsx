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
import { Grid, Typography } from "@material-ui/core";
import { default as React, FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AvailableAlgorithmOption } from "../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.interfaces";
import { ThresholdSetup } from "../../../components/alert-wizard-v3/threshold-setup/threshold-setup.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { PageContentsGridV1 } from "../../../platform/components";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { AppRouteRelative } from "../../../utils/routes/routes.util";

export const SetupMonitoringPage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();

    const { alert, onAlertPropertyChange, selectedAlgorithmOption } =
        useOutletContext<{
            alert: EditableAlert;
            onAlertPropertyChange: (
                contents: Partial<EditableAlert>,
                isTotalChange?: boolean
            ) => void;
            selectedAlgorithmOption: AvailableAlgorithmOption;
        }>();

    useEffect(() => {
        // On initial render, ensure there is already an alert template selected
        if (!alert.template?.name) {
            navigate(`../${AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE}`);
        }
    }, []);

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <Typography variant="h5">
                        {t("label.alert-setup")}
                    </Typography>
                    <Typography variant="body1">
                        {t("message.alert-setup-description")}
                    </Typography>
                </Grid>
                <Grid item xs={12}>
                    <ThresholdSetup
                        alert={alert}
                        algorithmOptionConfig={selectedAlgorithmOption}
                        onAlertPropertyChange={onAlertPropertyChange}
                    />
                </Grid>
            </PageContentsGridV1>

            <WizardBottomBar
                backBtnLink={
                    selectedAlgorithmOption.algorithmOption
                        .alertTemplateForMultidimension === alert.template?.name
                        ? `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION}`
                        : `../${AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE}`
                }
                nextBtnLink={`../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS}`}
            />
        </>
    );
};
