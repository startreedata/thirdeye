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
import { Box, Button, Grid, ThemeProvider } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext } from "react-router-dom";
import { AlertDetails } from "../../components/alert-wizard-v2/alert-details/alert-details-v2.component";
import { AlertNotifications } from "../../components/alert-wizard-v2/alert-notifications/alert-notifications.component";
import { AlertTemplate } from "../../components/alert-wizard-v2/alert-template/alert-template-v2.component";
import { PageContentsGridV1 } from "../../platform/components";
import {
    createAlertPageTheme,
    easyAlertStyles,
} from "../alerts-create-page/alerts-create-easy-page/alerts-create-easy-page.styles";
import { AlertsSimpleAdvancedJsonContainerPageOutletContextProps } from "../alerts-edit-create-common/alerts-edit-create-common-page.interfaces";

export const AlertsUpdateAdvancedPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const [submitBtnLabel, setSubmitBtnLabel] = useState<string>(
        t("label.update-entity", {
            entity: t("label.alert"),
        })
    );
    const [isSubmitBtnEnabled, setIsSubmitBtnEnabled] = useState(false);
    const {
        alert,
        handleAlertPropertyChange: onAlertPropertyChange,
        selectedAlertTemplate,
        setSelectedAlertTemplate,
        alertTemplateOptions,
        isEditRequestInFlight,
        handleSubmitAlertClick,
        onPageExit,
        selectedSubscriptionGroups,
        handleSubscriptionGroupChange: onSubscriptionGroupsChange,
    } = useOutletContext<AlertsSimpleAdvancedJsonContainerPageOutletContextProps>();

    const classes = easyAlertStyles();

    useEffect(() => {
        setIsSubmitBtnEnabled(false);
        setSubmitBtnLabel(
            t("message.preview-alert-in-chart-before-submitting")
        );
    }, []);

    return (
        <ThemeProvider theme={createAlertPageTheme}>
            <Box className={classes.backgroundContainer}>
                <PageContentsGridV1>
                    <Grid item xs={12}>
                        <AlertDetails
                            alert={alert}
                            onAlertPropertyChange={onAlertPropertyChange}
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <AlertTemplate
                            alert={alert}
                            alertTemplateOptions={alertTemplateOptions}
                            selectedAlertTemplate={selectedAlertTemplate}
                            setSelectedAlertTemplate={setSelectedAlertTemplate}
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
                    </Grid>
                    <Grid item xs={12}>
                        <AlertNotifications
                            alert={alert}
                            initiallySelectedSubscriptionGroups={
                                selectedSubscriptionGroups
                            }
                            onSubscriptionGroupsChange={
                                onSubscriptionGroupsChange
                            }
                        />
                    </Grid>
                </PageContentsGridV1>

                <Box
                    display="flex"
                    gridColumnGap={12}
                    marginBottom={3}
                    marginLeft={1}
                    marginTop={3}
                >
                    <Button
                        className={classes.button}
                        color="primary"
                        size="small"
                        variant="outlined"
                        onClick={onPageExit}
                    >
                        {t("label.cancel")}
                    </Button>
                    <Button
                        className={classes.button}
                        color="primary"
                        disabled={!isSubmitBtnEnabled || isEditRequestInFlight}
                        size="small"
                        onClick={() => {
                            handleSubmitAlertClick(alert);
                        }}
                    >
                        <Box component="span" display="flex" mr={0.5}>
                            <Icon
                                fontSize={16}
                                icon="mdi:check-circle-outline"
                            />
                        </Box>
                        <Box component="span">{submitBtnLabel}</Box>
                    </Button>
                </Box>
            </Box>
        </ThemeProvider>
    );
};
