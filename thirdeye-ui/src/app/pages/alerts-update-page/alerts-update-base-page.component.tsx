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
import { Box } from "@material-ui/core";
import { useQuery } from "@tanstack/react-query";
import { AxiosError } from "axios";
import { differenceBy, isEmpty, some, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { validateSubscriptionGroup } from "../../components/subscription-group-wizard/subscription-group-wizard.utils";
import {
    AppLoadingIndicatorV1,
    NotificationDisplayV1,
    NotificationTypeV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import {
    ALERT_CACHE_KEYS,
    getAlert,
    updateAlert,
} from "../../rest/alerts/alerts.rest";
import { Alert, EditableAlert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createSubscriptionGroup,
    getAllSubscriptionGroups,
    SUBSCRIPTION_GROUP_CACHE_KEYS,
    updateSubscriptionGroups,
} from "../../rest/subscription-groups/subscription-groups.rest";
import {
    notifyErrors,
    notifyIfErrors,
} from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getAlertsAlertPath } from "../../utils/routes/routes.util";
import {
    createEmptySubscriptionGroup,
    getSubscriptionGroupAlertsList,
} from "../../utils/subscription-groups/subscription-groups.util";
import { AlertsEditCreateBasePageComponent } from "../alerts-edit-create-common/alerts-edit-create-base-page.component";
import { AlertsUpdatePageParams } from "./alerts-update-page.interfaces";

export const AlertsUpdateBasePage: FunctionComponent = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { notify } = useNotificationProviderV1();
    const params = useParams<AlertsUpdatePageParams>();

    const {
        data: subscriptionGroups,
        isFetching: isSubscriptionGroupsRequestFetching,
    } = useQuery({
        queryKey: [SUBSCRIPTION_GROUP_CACHE_KEYS.GET_ALL_SUBSCRIPTION_GROUPS],
        queryFn: getAllSubscriptionGroups,
        onError: (err: AxiosError) => {
            notifyErrors(
                getErrorMessages(err),
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.subscription-groups"),
                })
            );
        },
    });

    const {
        data: originalAlert,
        isFetching: isAlertsRequestFetching,
        isError: isAlertsRequestError,
    } = useQuery({
        queryKey: [ALERT_CACHE_KEYS.GET_ALERT, toNumber(params.id)],
        queryFn: () => getAlert(toNumber(params.id)),
        onError: (err: AxiosError) => {
            notifyErrors(
                getErrorMessages(err),
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.alert"),
                })
            );
        },
    });

    const [
        currentlySelectedSubscriptionGroups,
        setCurrentlySelectedSubscriptionGroups,
    ] = useState<SubscriptionGroup[]>([]);
    const [
        originallySelectedSubscriptionGroups,
        setOriginallySelectedSubscriptionGroups,
    ] = useState<SubscriptionGroup[]>([]);

    const [isEditRequestInFlight, setIsEditRequestInFlight] = useState(false);

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

        if (originalAlert && subscriptionGroups) {
            const currentAlertId = originalAlert.id;
            const subGroupsAlertIsIn = subscriptionGroups.filter((subGroup) => {
                const alerts = getSubscriptionGroupAlertsList(subGroup);

                return some(alerts.map((alert) => alert.id === currentAlertId));
            });
            setOriginallySelectedSubscriptionGroups(
                subGroupsAlertIsIn as SubscriptionGroup[]
            );
            setCurrentlySelectedSubscriptionGroups(
                subGroupsAlertIsIn as SubscriptionGroup[]
            );
            singleNewSubscriptionGroup.name = `${originalAlert.name}_subscription_group`;
            setSingleNewSubscriptionGroup({
                ...singleNewSubscriptionGroup,
            });
        }
    }, [subscriptionGroups, originalAlert]);

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
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t(
                        "message.experienced-error-creating-subscription-group-while-creating-alert"
                    )
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
                alerts: getSubscriptionGroupAlertsList(
                    subscriptionGroup
                ).filter(
                    (subGroupAlert) => subGroupAlert.id !== alert.id // Remove alert from list
                ),
            })
        );

        const subscriptionGroupsToBeUpdated = [
            ...subscriptionGroupsToBeAdded,
            ...subscriptionGroupsToBeOmitted,
        ];

        try {
            await updateSubscriptionGroups(
                subscriptionGroupsToBeUpdated as SubscriptionGroup[]
            );
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", {
                    entity: t("label.subscription-groups"),
                })
            );
        } catch (error) {
            notifyIfErrors(
                ActionStatus.Error,
                getErrorMessages(error),
                notify,
                t("message.update-error", {
                    entity: t("label.subscription-groups"),
                })
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

        setIsEditRequestInFlight(true);
        updateAlert(modifiedAlert as Alert)
            .then((alert: Alert): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", { entity: t("label.alert") })
                );
                handleUpdatingSubscriptionGroups(alert);
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.update-error", {
                        entity: t("label.alert"),
                    })
                );
            })
            .finally(() => {
                setIsEditRequestInFlight(false);
            });
    };

    return (
        <LoadingErrorStateSwitch
            wrapInCard
            wrapInGrid
            wrapInGridContainer
            errorState={
                <PageV1>
                    <NotificationDisplayV1 />
                    <Box pb={20} pt={20}>
                        <NoDataIndicator />
                    </Box>
                </PageV1>
            }
            isError={isAlertsRequestError}
            isLoading={
                isSubscriptionGroupsRequestFetching && isAlertsRequestFetching
            }
            loadingState={<AppLoadingIndicatorV1 />}
        >
            <AlertsEditCreateBasePageComponent
                isEditRequestInFlight={isEditRequestInFlight}
                newSubscriptionGroup={singleNewSubscriptionGroup}
                pageTitle={t("label.update-entity", {
                    entity: t("label.alert"),
                })}
                selectedSubscriptionGroups={currentlySelectedSubscriptionGroups}
                startingAlertConfiguration={originalAlert as EditableAlert}
                onNewSubscriptionGroupChange={setSingleNewSubscriptionGroup}
                onSubmit={handleUpdateAlertClick}
                onSubscriptionGroupChange={
                    setCurrentlySelectedSubscriptionGroups
                }
            />
        </LoadingErrorStateSwitch>
    );
};
