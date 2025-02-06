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
import { Icon } from "@iconify/react";
import { Box, Button, Divider, Grid, ThemeProvider } from "@material-ui/core";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AlertJson } from "../../../components/alert-wizard-v2/alert-json-v2/alert-json.component";
import { AlertNotifications } from "../../../components/alert-wizard-v2/alert-notifications/alert-notifications.component";
import {
    determinePropertyFieldConfiguration,
    hasRequiredPropertyValuesSet,
} from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { PreviewChart } from "../../../components/alert-wizard-v2/alert-template/preview-chart/preview-chart-v2.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../../platform/components";
import { getAlertsAllPath } from "../../../utils/routes/routes.util";
import { AlertsSimpleAdvancedJsonContainerPageOutletContextProps } from "../../alerts-edit-create-common/alerts-edit-create-common-page.interfaces";
import { createAlertPageTheme } from "../alerts-create-easy-page/alerts-create-easy-page.styles";
import { alertJsonPageStyles } from "./alerts-create-json-page-v2.styles";

export const AlertsCreateJSONPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const classes = alertJsonPageStyles();
    const navigate = useNavigate();

    const {
        alert,
        handleAlertPropertyChange: onAlertPropertyChange,
        selectedAlertTemplate,
        handleSubscriptionGroupChange: onSubscriptionGroupsChange,
        handleSubmitAlertClick,
    } = useOutletContext<AlertsSimpleAdvancedJsonContainerPageOutletContextProps>();

    const [isAlertValid, setIsAlertValid] = React.useState(false);

    const availableFields = useMemo(() => {
        if (selectedAlertTemplate) {
            return determinePropertyFieldConfiguration(selectedAlertTemplate);
        }

        return [];
    }, [selectedAlertTemplate]);

    const areBasicFieldsFilled = useMemo(() => {
        return (
            !!selectedAlertTemplate &&
            hasRequiredPropertyValuesSet(
                availableFields,
                alert.templateProperties
            )
        );
    }, [availableFields, alert]);

    return (
        <>
            <ThemeProvider theme={createAlertPageTheme}>
                <PageContentsGridV1>
                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            <AlertJson
                                alert={alert}
                                setIsAlertValid={setIsAlertValid}
                                onAlertPropertyChange={onAlertPropertyChange}
                            />
                            <Box paddingTop={2}>
                                <PreviewChart
                                    alert={alert}
                                    disableReload={!isAlertValid}
                                    hideCallToActionPrompt={
                                        areBasicFieldsFilled
                                    }
                                    onAlertPropertyChange={
                                        onAlertPropertyChange
                                    }
                                />
                            </Box>
                            <Box marginBottom={3} marginTop={3}>
                                <Divider />
                            </Box>
                            <Box>
                                <AlertNotifications
                                    alert={alert}
                                    initiallySelectedSubscriptionGroups={[]}
                                    onSubscriptionGroupsChange={
                                        onSubscriptionGroupsChange
                                    }
                                />
                            </Box>
                            <Box
                                display="flex"
                                gridColumnGap={12}
                                marginBottom={3}
                                marginTop={3}
                            >
                                <Button
                                    className={classes.button}
                                    color="primary"
                                    size="small"
                                    variant="outlined"
                                    onClick={() => {
                                        navigate(getAlertsAllPath());
                                    }}
                                >
                                    {t("label.cancel")}
                                </Button>
                                <Button
                                    className={classes.button}
                                    color="primary"
                                    size="small"
                                    onClick={() => {
                                        handleSubmitAlertClick(alert);
                                    }}
                                >
                                    <Box
                                        component="span"
                                        display="flex"
                                        mr={0.5}
                                    >
                                        <Icon
                                            fontSize={16}
                                            icon="mdi:check-circle-outline"
                                        />
                                    </Box>
                                    <Box component="span">
                                        {t("label.create-alert")}
                                    </Box>
                                </Button>
                            </Box>
                        </PageContentsCardV1>
                    </Grid>
                </PageContentsGridV1>
            </ThemeProvider>
        </>
    );
};
