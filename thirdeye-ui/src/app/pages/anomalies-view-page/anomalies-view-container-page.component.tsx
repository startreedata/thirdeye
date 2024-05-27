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
import { Box, Card, CardContent, Grid } from "@material-ui/core";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useParams } from "react-router-dom";
import { PageSkeleton } from "../../components/anomalies-view/page-skeleton/page-skeleton.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlertInsight } from "../../rest/alerts/alerts.actions";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { Anomaly, AnomalyFeedback } from "../../rest/dto/anomaly.interfaces";
import { useGetEnumerationItem } from "../../rest/enumeration-items/enumeration-items.actions";
import { useGetInvestigations } from "../../rest/rca/rca.actions";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { AnomaliesViewPageParams } from "./anomalies-view-page.interfaces";

export const AnomaliesViewContainerPage: FunctionComponent = () => {
    const { id: anomalyId } = useParams<AnomaliesViewPageParams>();
    const { enumerationItem, getEnumerationItem } = useGetEnumerationItem();
    const {
        anomaly: fetchedAnomaly,
        getAnomaly,
        status: anomalyRequestStatus,
        errorMessages: anomalyRequestErrors,
    } = useGetAnomaly();
    const {
        investigations,
        getInvestigations,
        status: getInvestigationsRequestStatus,
    } = useGetInvestigations();
    const { alertInsight, getAlertInsight } = useGetAlertInsight();
    const [currentAnomaly, setCurrentAnomaly] = useState<Anomaly>();
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();

    useEffect(() => {
        !!fetchedAnomaly &&
            fetchedAnomaly.alert?.id &&
            getAlertInsight({ alertId: fetchedAnomaly.alert.id });
    }, [fetchedAnomaly]);

    useEffect(() => {
        anomalyId &&
            isValidNumberId(anomalyId) &&
            getAnomaly(toNumber(anomalyId)).then(
                (anomaly: Anomaly | undefined) => {
                    anomaly && setCurrentAnomaly(anomaly);
                }
            );
        anomalyId && getInvestigations(Number(anomalyId));
    }, [anomalyId]);

    useEffect(() => {
        /**
         * If anomaly has an enumeration id, fetch the enumeration item so
         * that we can use the correct detection evaluation
         */
        !!currentAnomaly &&
            currentAnomaly.enumerationItem &&
            getEnumerationItem(currentAnomaly.enumerationItem.id);
    }, [currentAnomaly]);

    useEffect(() => {
        notifyIfErrors(
            anomalyRequestStatus,
            anomalyRequestErrors,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.anomaly"),
            })
        );
    }, [anomalyRequestStatus, anomalyRequestErrors]);

    if (anomalyId && !isValidNumberId(anomalyId)) {
        // Invalid id
        notify(
            NotificationTypeV1.Error,
            t("message.invalid-id", {
                entity: t("label.anomaly"),
                id: anomalyId,
            })
        );
    }

    const handleFeedbackUpdateSuccess = (
        updatedFeedback: AnomalyFeedback
    ): void => {
        if (!currentAnomaly) {
            return;
        }

        const copied = { ...currentAnomaly, feedback: updatedFeedback };
        setCurrentAnomaly(copied);
    };

    return (
        <LoadingErrorStateSwitch
            wrapInGrid
            wrapInGridContainer
            errorState={
                <PageV1>
                    <PageContentsGridV1>
                        <Grid item xs={12}>
                            <Card>
                                <CardContent>
                                    <Box p={20}>
                                        <NoDataIndicator
                                            text={
                                                isEmpty(anomalyRequestErrors)
                                                    ? ""
                                                    : anomalyRequestErrors[0]
                                            }
                                        />
                                    </Box>
                                </CardContent>
                            </Card>
                        </Grid>
                    </PageContentsGridV1>
                </PageV1>
            }
            isError={anomalyRequestStatus === ActionStatus.Error}
            isLoading={
                anomalyRequestStatus === ActionStatus.Initial ||
                anomalyRequestStatus === ActionStatus.Working
            }
            loadingState={<PageSkeleton />}
        >
            <Outlet
                context={{
                    anomaly: fetchedAnomaly,
                    enumerationItem,
                    handleFeedbackUpdateSuccess,
                    alertInsight,
                    getInvestigationsRequestStatus,
                    investigations,
                }}
            />
        </LoadingErrorStateSwitch>
    );
};
