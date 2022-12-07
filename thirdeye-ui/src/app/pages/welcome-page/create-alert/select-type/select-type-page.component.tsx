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
import { default as React, FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AlgorithmSelection } from "../../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.component";
import { AvailableAlgorithmOption } from "../../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.interfaces";
import { SampleAlertSelection } from "../../../../components/alert-wizard-v3/sample-alert-selection/sample-alert-selection.component";
import { NoDataIndicator } from "../../../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../../../components/page-states/empty-state-switch/empty-state-switch.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../../../platform/components";
import { createDefaultAlertTemplates } from "../../../../rest/alert-templates/alert-templates.rest";
import { EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { AppRouteRelative } from "../../../../utils/routes/routes.util";

export const SelectTypePage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();

    const {
        handleAlertPropertyChange,
        simpleOptions,
        advancedOptions,
        getAlertTemplates,
    } = useOutletContext<{
        alert: EditableAlert;
        handleAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
        simpleOptions: AvailableAlgorithmOption[];
        advancedOptions: AvailableAlgorithmOption[];
        getAlertTemplates: () => void;
    }>();

    const handleAlgorithmSelection = (
        isDimensionExploration: boolean
    ): void => {
        if (isDimensionExploration) {
            navigate(
                `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION}`
            );

            return;
        }

        navigate(
            `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`
        );
    };

    const handleCreateDefaultAlertTemplates = (): void => {
        createDefaultAlertTemplates().then(() => {
            getAlertTemplates();
        });
    };

    return (
        <PageContentsGridV1>
            <Grid item xs={12}>
                <Typography variant="h5">
                    {t("message.select-alert-type")}
                </Typography>
                <Typography variant="body1">
                    {t(
                        "message.this-is-the-detector-algorithm-that-will-rule-alert"
                    )}
                </Typography>
            </Grid>
            <Grid item xs={12}>
                <SampleAlertSelection
                    onSampleAlertSelect={handleAlertPropertyChange}
                />
            </Grid>
            <Grid item xs={12}>
                <EmptyStateSwitch
                    emptyState={
                        <PageContentsCardV1>
                            <Box padding={10}>
                                <NoDataIndicator>
                                    <Box textAlign="center">
                                        {t("message.no-entity-created", {
                                            entity: t("label.alert-templates"),
                                        })}
                                    </Box>
                                    <Box marginTop={2} textAlign="center">
                                        <Button
                                            color="primary"
                                            onClick={
                                                handleCreateDefaultAlertTemplates
                                            }
                                        >
                                            {t("label.load-defaults")}
                                        </Button>
                                    </Box>
                                </NoDataIndicator>
                            </Box>
                        </PageContentsCardV1>
                    }
                    isEmpty={
                        advancedOptions.length === 0 &&
                        simpleOptions.length === 0
                    }
                >
                    <AlgorithmSelection
                        advancedOptions={advancedOptions}
                        simpleOptions={simpleOptions}
                        onAlertPropertyChange={handleAlertPropertyChange}
                        onSelectionComplete={handleAlgorithmSelection}
                    />
                </EmptyStateSwitch>
            </Grid>
        </PageContentsGridV1>
    );
};
