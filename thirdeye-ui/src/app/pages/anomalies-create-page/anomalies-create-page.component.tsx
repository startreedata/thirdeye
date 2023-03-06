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

import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { CreateAnomalyWizard } from "../../components/anomalies-create/create-anomaly-wizard/create-anomaly-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { PageHeaderProps } from "../../components/page-header/page-header.interfaces";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    NotificationTypeV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../platform/rest/actions.interfaces";
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import { createAnomaly } from "../../rest/anomalies/anomalies.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getAlertsAlertViewPath,
    getAnomaliesAllPath,
    getAnomaliesCreatePath,
    getAnomaliesViewPath,
} from "../../utils/routes/routes.util";
import { EditedAnomaly } from "./anomalies-create-page.interfaces";
import { createEmptyAnomaly } from "./anomalies-create-page.utils";

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

    const handleAnomalyCreate = (editedAnomaly: EditedAnomaly): void => {
        createAnomaly(editedAnomaly)
            .then((data) => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", { entity: t("label.anomaly") })
                );
                // TODO: Remove
                window.open(getAnomaliesViewPath(data.id), "_blank");
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

    const initialAnomaly = useMemo(() => createEmptyAnomaly(), []);

    const pageHeaderProps: PageHeaderProps = {
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
            ...(selectedAlert
                ? [
                      {
                          link: getAlertsAlertViewPath(selectedAlert.id),
                          label: selectedAlert.name,
                      },
                  ]
                : []),
        ],
    };

    return (
        <PageV1>
            <PageHeader {...pageHeaderProps} />

            <LoadingErrorStateSwitch
                isError={alertsStatus === ActionStatus.Error}
                isLoading={alertsStatus === ActionStatus.Working}
            >
                <CreateAnomalyWizard
                    alerts={alerts as Alert[]}
                    cancelBtnLabel={t("label.back")}
                    initialAnomalyData={initialAnomaly}
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
