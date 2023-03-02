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
import { useNavigate } from "react-router-dom";
import { CreateAnomalyWizard } from "../../components/anomalies-create/create-anomaly-wizard/create-anomaly-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { PageHeaderProps } from "../../components/page-header/page-header.interfaces";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { PageV1 } from "../../platform/components";
import { ActionStatus } from "../../platform/rest/actions.interfaces";
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    getAnomaliesAllPath,
    getAnomaliesCreatePath,
} from "../../utils/routes/routes.util";
import { EditedAnomaly } from "./anomalies-create-page.interfaces";
import { createEmptyAnomaly } from "./anomalies-create-page.utils";

export const AnomaliesCreatePage: FunctionComponent = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { alerts, getAlerts, status: alertsStatus } = useGetAlerts();

    useEffect(() => {
        getAlerts();
    }, []);

    const handleCancelClick = (): void => {
        navigate(getAnomaliesAllPath());
    };

    const handleAnomalyCreate = (editedAnomaly: EditedAnomaly): void => {
        editedAnomaly;
        // console.log({ editedAnomaly });
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
