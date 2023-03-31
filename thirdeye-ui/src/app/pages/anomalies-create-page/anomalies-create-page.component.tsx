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

import { Icon } from "@iconify/react";
import { Box, Button, Divider, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { CreateAnomalyWizard } from "../../components/anomalies-create/create-anomaly-wizard/create-anomaly-wizard.component";
import { anomalyCreateBasicHelpCards } from "../../components/help-drawer-v1/help-drawer-card-contents.utils";
import { HelpDrawerV1 } from "../../components/help-drawer-v1/help-drawer-v1.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { PageHeaderProps } from "../../components/page-header/page-header.interfaces";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    NotificationScopeV1,
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../platform/rest/actions.interfaces";
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import { createAnomaly } from "../../rest/anomalies/anomalies.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { EditableAnomaly } from "../../rest/dto/anomaly.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getAlertsAlertViewPath,
    getAnomaliesAllPath,
    getAnomaliesCreatePath,
    getAnomaliesViewPath,
} from "../../utils/routes/routes.util";

export const AnomaliesCreatePage: FunctionComponent = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { notify } = useNotificationProviderV1();
    const { alerts, getAlerts, status: alertsStatus } = useGetAlerts();
    const { id: selectedAlertId } = useParams<{ id?: string }>();

    useEffect(() => {
        getAlerts();
    }, []);

    const selectedAlert = useMemo<Alert | null>(() => {
        if (alerts) {
            const searchedAlert = alerts.find(
                (a) => a.id === Number(selectedAlertId)
            );
            if (searchedAlert) {
                return searchedAlert;
            }
        }

        return null;
    }, [alerts, selectedAlertId]);

    const handleCancelClick = (): void => {
        navigate(getAnomaliesAllPath());
    };

    const handleAnomalyCreate = (editableAnomaly: EditableAnomaly): void => {
        createAnomaly(editableAnomaly)
            .then((data) => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", { entity: t("label.anomaly") }),
                    false,
                    NotificationScopeV1.Global
                );
                navigate(getAnomaliesViewPath(data.id));
            })
            .catch((error) => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.create-error", {
                        entity: t("label.alert"),
                    })
                );
            });
    };

    const pageHeaderProps = useMemo<PageHeaderProps>(() => {
        const pageHeader: PageHeaderProps = {
            title: t("label.report-missed-anomaly"),
            breadcrumbs: [
                {
                    link: getAnomaliesAllPath(),
                    label: t("label.anomalies"),
                },
                {
                    link: getAnomaliesCreatePath(),
                    label: t("label.create"),
                },
            ],
            customActions: (
                <PageHeaderActionsV1>
                    <HelpDrawerV1
                        cards={anomalyCreateBasicHelpCards}
                        title={`${t("label.need-help")}?`}
                        trigger={(handleOpen) => (
                            <Button
                                color="primary"
                                size="small"
                                variant="outlined"
                                onClick={handleOpen}
                            >
                                <Box component="span" mr={1}>
                                    {t("label.need-help")}
                                </Box>
                                <Box component="span" display="flex">
                                    <Icon
                                        fontSize={24}
                                        icon="mdi:question-mark-circle-outline"
                                    />
                                </Box>
                            </Button>
                        )}
                    />
                </PageHeaderActionsV1>
            ),
        };

        // Add the selected alert to the breadcrumbs to reflect the URL structure
        if (selectedAlert) {
            pageHeader.breadcrumbs?.push({
                link: getAlertsAlertViewPath(selectedAlert.id),
                label: selectedAlert.name,
            });
        }

        return pageHeader;
    }, [selectedAlert]);

    return (
        <PageV1>
            <PageHeader {...pageHeaderProps} />

            <LoadingErrorStateSwitch
                isError={alertsStatus === ActionStatus.Error}
                isLoading={[
                    ActionStatus.Initial,
                    ActionStatus.Working,
                ].includes(alertsStatus)}
                loadingState={
                    <PageContentsGridV1 fullHeight>
                        <Grid item xs={12}>
                            <PageContentsCardV1 fullHeight>
                                <Grid container alignItems="stretch">
                                    <Grid item xs={12}>
                                        <Typography variant="h5">
                                            {t("label.setup-entity", {
                                                entity: t("label.anomaly"),
                                            })}
                                        </Typography>
                                        <Typography
                                            color="secondary"
                                            variant="subtitle1"
                                        >
                                            {t(
                                                "message.configure-the-parent-alert-and-the-occurrence-date-time-for-the-anomalous-behavior"
                                            )}
                                        </Typography>
                                    </Grid>
                                    <Grid item xs={12}>
                                        <SkeletonV1
                                            height={250}
                                            variant="rect"
                                            width="100%"
                                        />
                                    </Grid>
                                    <Grid item xs={12}>
                                        <Box py={1}>
                                            <Divider />
                                        </Box>
                                    </Grid>
                                    <Grid item xs={12}>
                                        <SkeletonV1
                                            height={350}
                                            variant="rect"
                                            width="100%"
                                        />
                                    </Grid>
                                </Grid>
                            </PageContentsCardV1>
                        </Grid>
                    </PageContentsGridV1>
                }
            >
                <CreateAnomalyWizard
                    alerts={alerts as Alert[]}
                    cancelBtnLabel={t("label.back")}
                    submitBtnLabel={t("label.save-entity", {
                        entity: t("label.anomaly"),
                    })}
                    onCancel={handleCancelClick}
                    onSubmit={handleAnomalyCreate}
                />
            </LoadingErrorStateSwitch>
        </PageV1>
    );
};
