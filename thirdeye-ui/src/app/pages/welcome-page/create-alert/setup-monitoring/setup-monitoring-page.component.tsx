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
import { Box, Button, Grid, Typography } from "@material-ui/core";
import { default as React, FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import {
    Link as RouterLink,
    useNavigate,
    useOutletContext,
} from "react-router-dom";
import { AvailableAlgorithmOption } from "../../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.interfaces";
import { ThresholdSetup } from "../../../../components/alert-wizard-v3/threshold-setup/threshold-setup.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../../../platform/components";
import { EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { AppRouteRelative } from "../../../../utils/routes/routes.util";

export const SetupMonitoringPage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();

    const { alert, handleAlertPropertyChange, selectedAlgorithmOption } =
        useOutletContext<{
            alert: EditableAlert;
            handleAlertPropertyChange: (
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
                        onAlertPropertyChange={handleAlertPropertyChange}
                    />
                </Grid>
            </PageContentsGridV1>

            <Box width="100%">
                <PageContentsCardV1>
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Button
                                color="secondary"
                                component={RouterLink}
                                to={
                                    selectedAlgorithmOption.algorithmOption
                                        .alertTemplateForMultidimension ===
                                    alert.template?.name
                                        ? `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION}`
                                        : `../${AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE}`
                                }
                            >
                                {t("label.back")}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Button
                                color="primary"
                                component={RouterLink}
                                to={`../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS}`}
                            >
                                {t("label.next")}
                            </Button>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Box>
        </>
    );
};
