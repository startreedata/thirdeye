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
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useOutletContext } from "react-router-dom";
import { AlertJson } from "../../components/alert-wizard-v2/alert-json-v2/alert-json.component";
import { AlertNotifications } from "../../components/alert-wizard-v2/alert-notifications/alert-notifications.component";
import { PreviewChart } from "../../components/alert-wizard-v2/alert-template/preview-chart/preview-chart.component";
import { GranularityValue } from "../../components/alert-wizard-v3/select-metric/select-metric.utils";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    useDialogProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { getAlertsAllPath } from "../../utils/routes/routes.util";
import { createAlertPageTheme } from "../alerts-create-page/alerts-create-easy-page/alerts-create-easy-page.styles";
import { alertJsonPageStyles } from "../alerts-create-page/alerts-create-json-page-v2/alerts-create-json-page-v2.styles";
import { AlertsSimpleAdvancedJsonContainerPageOutletContextProps } from "../alerts-edit-create-common/alerts-edit-create-common-page.interfaces";

export const AlertsUpdateJSONPageV2: FunctionComponent = () => {
    const { t } = useTranslation();

    const { showDialog } = useDialogProviderV1();
    const classes = alertJsonPageStyles();
    const navigate = useNavigate();

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
        handleSubmitAlertClick,
        isEditRequestInFlight,
    } = useOutletContext<AlertsSimpleAdvancedJsonContainerPageOutletContextProps>();

    useEffect(() => {
        setIsSubmitBtnEnabled(false);
        setSubmitBtnLabel(
            t("message.preview-alert-in-chart-before-submitting")
        );
    }, []);

    const onCreateAlertClick: () => void = async () => {
        if (
            alert.templateProperties.monitoringGranularity &&
            (alert.templateProperties.monitoringGranularity ===
                GranularityValue.ONE_MINUTE ||
                alert.templateProperties.monitoringGranularity ===
                    GranularityValue.FIVE_MINUTES)
        ) {
            showDialog({
                type: DialogType.ALERT,
                contents: t(
                    "message.monitoring-granularity-below-15-minutes-warning"
                ),
                okButtonText: t("label.confirm"),
                cancelButtonText: t("label.cancel"),
                onOk: () => handleSubmitAlertClick(alert),
            });
        } else {
            handleSubmitAlertClick(alert);
        }
    };

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
                            <Box marginBottom={3} marginTop={3}>
                                <Divider />
                            </Box>
                            <Box>
                                <PreviewChart
                                    alert={alert}
                                    disableReload={!isAlertValid}
                                    onAlertPropertyChange={
                                        onAlertPropertyChange
                                    }
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
                            onSubscriptionGroupsChange={
                                onSubscriptionGroupsChange
                            }
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginBottom={3} marginTop={3}>
                            <Divider />
                        </Box>
                        <Box
                            display="flex"
                            gridColumnGap={12}
                            marginBottom={3}
                            marginTop={3}
                        >
                            <Button
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
                                disabled={
                                    !isSubmitBtnEnabled ||
                                    isEditRequestInFlight ||
                                    !isAlertValid
                                }
                                size="small"
                                onClick={() => {
                                    onCreateAlertClick();
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
                    </Grid>
                </PageContentsGridV1>
            </ThemeProvider>
        </>
    );
};
