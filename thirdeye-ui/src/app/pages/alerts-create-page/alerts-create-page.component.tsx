/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";
import { AlertWizard } from "../../components/alert-wizard/alert-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import { createAlert, getAllAlerts } from "../../rest/alerts/alerts.rest";
import {
    Alert,
    AlertEvaluation,
    EditableAlert,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createSubscriptionGroup,
    getAllSubscriptionGroups,
    updateSubscriptionGroups,
} from "../../rest/subscription-groups/subscription-groups.rest";
import {
    createAlertEvaluation,
    createDefaultAlert,
} from "../../utils/alerts/alerts.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    generateDateRangeMonthsFromNow,
    getAlertsViewPath,
} from "../../utils/routes/routes.util";

export const AlertsCreatePage: FunctionComponent = () => {
    const {
        getEvaluation,
        errorMessages,
        status: getEvaluationStatus,
    } = useGetEvaluation();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );

    useEffect(() => {
        // Default the time range to 6 months ago
        const [defaultStart, defaultEnd] = generateDateRangeMonthsFromNow(6);
        searchParams.set(
            TimeRangeQueryStringKey.START_TIME,
            defaultStart.toString()
        );
        searchParams.set(
            TimeRangeQueryStringKey.END_TIME,
            defaultEnd.toString()
        );
        setSearchParams(searchParams, { replace: true });
    }, []);

    const onAlertWizardFinish = (
        alert: EditableAlert,
        subscriptionGroups: SubscriptionGroup[]
    ): void => {
        if (!alert) {
            return;
        }

        createAlert(alert)
            .then((alert: Alert): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", { entity: t("label.alert") })
                );

                if (isEmpty(subscriptionGroups)) {
                    // Redirect to alerts detail path
                    navigate(getAlertsViewPath(alert.id));

                    return;
                }

                // Update subscription groups with new alert
                for (const subscriptionGroup of subscriptionGroups) {
                    if (subscriptionGroup.alerts) {
                        // Add to existing list
                        subscriptionGroup.alerts.push(alert);
                    } else {
                        // Create and add to list
                        subscriptionGroup.alerts = [alert];
                    }
                }

                updateSubscriptionGroups(subscriptionGroups)
                    .then((): void => {
                        notify(
                            NotificationTypeV1.Success,
                            t("message.update-success", {
                                entity: t("label.subscription-groups"),
                            })
                        );
                    })
                    .finally((): void => {
                        // Redirect to alerts detail path
                        navigate(getAlertsViewPath(alert.id));
                    });
            })
            .catch((error: AxiosError) => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.create-error", {
                              entity: t("label.alert"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };

    const onSubscriptionGroupWizardFinish = async (
        subscriptionGroup: SubscriptionGroup
    ): Promise<SubscriptionGroup> => {
        let newSubscriptionGroup: SubscriptionGroup =
            null as unknown as SubscriptionGroup;

        if (!subscriptionGroup) {
            return newSubscriptionGroup;
        }

        try {
            newSubscriptionGroup = await createSubscriptionGroup(
                subscriptionGroup
            );

            notify(
                NotificationTypeV1.Success,
                t("message.create-success", {
                    entity: t("label.subscription-group"),
                })
            );
        } catch (error) {
            const errMessages = getErrorMessages(error as AxiosError);

            isEmpty(errMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.create-error", {
                          entity: t("label.subscription-group"),
                      })
                  )
                : errMessages.map((err) =>
                      notify(NotificationTypeV1.Error, err)
                  );
        }

        return newSubscriptionGroup;
    };

    const fetchAllSubscriptionGroups = async (): Promise<
        SubscriptionGroup[]
    > => {
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];
        try {
            fetchedSubscriptionGroups = await getAllSubscriptionGroups();
        } catch (error) {
            const errMessages = getErrorMessages(error as AxiosError);

            isEmpty(errMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.subscription-groups"),
                      })
                  )
                : errMessages.map((err) =>
                      notify(NotificationTypeV1.Error, err)
                  );
        }

        return fetchedSubscriptionGroups;
    };

    const fetchAllAlerts = async (): Promise<Alert[]> => {
        let fetchedAlerts: Alert[] = [];
        try {
            fetchedAlerts = await getAllAlerts();
        } catch (error) {
            const errMessages = getErrorMessages(error as AxiosError);

            isEmpty(errMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.alerts"),
                      })
                  )
                : errMessages.map((err) =>
                      notify(NotificationTypeV1.Error, err)
                  );
        }

        return fetchedAlerts;
    };

    // Respect start & end time provided
    // fix for: TE-678 (https://cortexdata.atlassian.net/browse/TE-678)
    const fetchAlertEvaluation = async (
        alert: EditableAlert,
        start?: number,
        end?: number
    ): Promise<AlertEvaluation> => {
        const params: [Alert | EditableAlert, number, number] = [
            alert,
            start ?? startTime,
            end ?? endTime,
        ];
        const fetchedAlertEvaluation = await getEvaluation(
            createAlertEvaluation(...params)
        );

        if (fetchedAlertEvaluation === undefined) {
            return {} as AlertEvaluation;
        }

        return fetchedAlertEvaluation;
    };

    useEffect(() => {
        if (getEvaluationStatus === ActionStatus.Error) {
            !isEmpty(errorMessages)
                ? errorMessages.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.chart-data"),
                      })
                  );
        }
    }, [errorMessages, getEvaluationStatus]);

    return (
        <PageV1>
            <PageHeader
                title={t("label.create-entity", {
                    entity: t("label.alert"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <AlertWizard<EditableAlert>
                        createNewMode
                        alert={createDefaultAlert()}
                        getAlertEvaluation={fetchAlertEvaluation}
                        getAllAlerts={fetchAllAlerts}
                        getAllSubscriptionGroups={fetchAllSubscriptionGroups}
                        onFinish={onAlertWizardFinish}
                        onSubscriptionGroupWizardFinish={
                            onSubscriptionGroupWizardFinish
                        }
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
