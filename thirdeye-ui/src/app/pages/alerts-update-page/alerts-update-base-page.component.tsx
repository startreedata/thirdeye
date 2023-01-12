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
import { AxiosError } from "axios";
import { differenceBy, isEmpty, some, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { validateSubscriptionGroup } from "../../components/subscription-group-wizard/subscription-group-whizard.utils";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlert } from "../../rest/alerts/alerts.actions";
import { updateAlert } from "../../rest/alerts/alerts.rest";
import { Alert, EditableAlert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { useGetSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.actions";
import {
    createSubscriptionGroup,
    updateSubscriptionGroups,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getAlertsAlertPath } from "../../utils/routes/routes.util";
import { createEmptySubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";
import { AlertsEditBasePage } from "./alerts-edit-base-page.component";
import { AlertsUpdatePageParams } from "./alerts-update-page.interfaces";

export const AlertsUpdateBasePage: FunctionComponent = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { notify } = useNotificationProviderV1();
    const {
        getSubscriptionGroups,
        errorMessages: getSubscriptionGroupsErrorMessages,
        status: getSubscriptionGroupStatus,
    } = useGetSubscriptionGroups();
    const [
        currentlySelectedSubscriptionGroups,
        setCurrentlySelectedSubscriptionGroups,
    ] = useState<SubscriptionGroup[]>([]);
    const [
        originallySelectedSubscriptionGroups,
        setOriginallySelectedSubscriptionGroups,
    ] = useState<SubscriptionGroup[]>([]);
    const {
        alert: originalAlert,
        getAlert,
        status: getAlertStatus,
        errorMessages: getAlertErrorMessages,
    } = useGetAlert();
    const params = useParams<AlertsUpdatePageParams>();
    const [loading, setLoading] = useState(true);

    const [singleNewSubscriptionGroup, setSingleNewSubscriptionGroup] =
        useState<SubscriptionGroup>(createEmptySubscriptionGroup());

    useEffect(() => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: params.id,
                })
            );

            return;
        }

        Promise.allSettled([
            getAlert(toNumber(params.id)),
            getSubscriptionGroups(),
        ])
            .then(([getAlertResult, getSubscriptionResult]) => {
                if (
                    getSubscriptionResult.status === "fulfilled" &&
                    getAlertResult.status === "fulfilled" &&
                    getSubscriptionResult.value &&
                    getAlertResult.value
                ) {
                    const currentAlertId = getAlertResult.value.id;
                    const subGroupsAlertIsIn =
                        getSubscriptionResult.value.filter((subGroup) => {
                            return some(
                                subGroup.alerts.map(
                                    (alert) => alert.id === currentAlertId
                                )
                            );
                        });
                    setOriginallySelectedSubscriptionGroups(
                        subGroupsAlertIsIn as SubscriptionGroup[]
                    );
                    setCurrentlySelectedSubscriptionGroups(
                        subGroupsAlertIsIn as SubscriptionGroup[]
                    );
                    singleNewSubscriptionGroup.name = `${getAlertResult.value.name}_subscription_group`;
                    setSingleNewSubscriptionGroup({
                        ...singleNewSubscriptionGroup,
                    });
                }
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    useEffect(() => {
        if (getAlertStatus !== ActionStatus.Error) {
            return;
        }

        isEmpty(getAlertErrorMessages)
            ? notify(
                  NotificationTypeV1.Error,
                  t("message.error-while-fetching", {
                      entity: t("label.alert"),
                  })
              )
            : getAlertErrorMessages.map((err) =>
                  notify(NotificationTypeV1.Error, err)
              );
    }, [getAlertErrorMessages]);

    useEffect(() => {
        if (getSubscriptionGroupStatus !== ActionStatus.Error) {
            return;
        }

        isEmpty(getSubscriptionGroupsErrorMessages)
            ? notify(
                  NotificationTypeV1.Error,
                  t("message.error-while-fetching", {
                      entity: t("label.subscription-groups"),
                  })
              )
            : getSubscriptionGroupsErrorMessages.map((err) =>
                  notify(NotificationTypeV1.Error, err)
              );
    }, [getSubscriptionGroupsErrorMessages]);

    const handleUpdatingSubscriptionGroups = async (
        alert: Alert
    ): Promise<void> => {
        let copiedCurrentlySelectedSubscriptionGroups = [
            ...currentlySelectedSubscriptionGroups,
        ];

        if (
            validateSubscriptionGroup(singleNewSubscriptionGroup) &&
            singleNewSubscriptionGroup.specs?.length > 0
        ) {
            try {
                const newlyCreatedSubGroup = await createSubscriptionGroup(
                    singleNewSubscriptionGroup
                );
                copiedCurrentlySelectedSubscriptionGroups = [
                    ...copiedCurrentlySelectedSubscriptionGroups,
                    newlyCreatedSubGroup,
                ];
            } catch (error) {
                const errMessages = getErrorMessages(error);

                notify(
                    NotificationTypeV1.Error,
                    t(
                        "message.experienced-error-creating-subscription-group-while-creating-alert"
                    )
                );
                !isEmpty(errMessages) &&
                    errMessages.map((err) =>
                        notify(NotificationTypeV1.Error, err)
                    );
            }
        }

        // Find any subscription groups in the original selected list
        // that does not exist in the currently selected list
        const subscriptionGroupsRemoved = differenceBy(
            originallySelectedSubscriptionGroups,
            copiedCurrentlySelectedSubscriptionGroups,
            "id"
        );
        const subscriptionGroupsAdded = differenceBy(
            copiedCurrentlySelectedSubscriptionGroups,
            originallySelectedSubscriptionGroups,
            "id"
        );

        if (
            isEmpty(subscriptionGroupsRemoved) &&
            isEmpty(subscriptionGroupsAdded)
        ) {
            // Redirect to alerts detail path
            navigate(getAlertsAlertPath(alert.id));

            return;
        }

        // Add alert to subscription groups
        const subscriptionGroupsToBeAdded = subscriptionGroupsAdded.map(
            (subscriptionGroup) => ({
                ...subscriptionGroup,
                alerts: subscriptionGroup.alerts
                    ? [...subscriptionGroup.alerts, alert] // Add to existing list
                    : [alert], // Create new list
            })
        );

        // Remove alert from subscription groups
        const subscriptionGroupsToBeOmitted = subscriptionGroupsRemoved.map(
            (subscriptionGroup) => ({
                ...subscriptionGroup,
                alerts: subscriptionGroup.alerts.filter(
                    (subGroupAlert) => subGroupAlert.id !== alert.id // Remove alert from list
                ),
            })
        );

        const subscriptionGroupsToBeUpdated = [
            ...subscriptionGroupsToBeAdded,
            ...subscriptionGroupsToBeOmitted,
        ];

        try {
            await updateSubscriptionGroups(subscriptionGroupsToBeUpdated);
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", {
                    entity: t("label.subscription-groups"),
                })
            );
        } catch (error) {
            const errMessages = getErrorMessages(error);

            isEmpty(errMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.update-error", {
                          entity: t("label.subscription-groups"),
                      })
                  )
                : errMessages.map((err) =>
                      notify(NotificationTypeV1.Error, err)
                  );
        } finally {
            // Redirect to alerts detail path
            navigate(getAlertsAlertPath(alert.id));
        }
    };

    const handleUpdateAlertClick = (modifiedAlert: EditableAlert): void => {
        if (!modifiedAlert) {
            return;
        }

        updateAlert(modifiedAlert as Alert)
            .then((alert: Alert): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", { entity: t("label.alert") })
                );
                handleUpdatingSubscriptionGroups(alert);
            })
            .catch((error: AxiosError): void => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.update-error", {
                              entity: t("label.alert"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };

    return (
        <LoadingErrorStateSwitch
            isError={!originalAlert}
            isLoading={loading}
            loadingState={<AppLoadingIndicatorV1 />}
        >
            <AlertsEditBasePage
                newSubscriptionGroup={singleNewSubscriptionGroup}
                pageTitle={t("label.update-entity", {
                    entity: t("label.alert"),
                })}
                selectedSubscriptionGroups={currentlySelectedSubscriptionGroups}
                startingAlertConfiguration={originalAlert as EditableAlert}
                submitButtonLabel={t("label.update-entity", {
                    entity: t("label.alert"),
                })}
                onNewSubscriptionGroupChange={setSingleNewSubscriptionGroup}
                onSubmit={handleUpdateAlertClick}
                onSubscriptionGroupChange={
                    setCurrentlySelectedSubscriptionGroups
                }
            />
        </LoadingErrorStateSwitch>
    );
};
