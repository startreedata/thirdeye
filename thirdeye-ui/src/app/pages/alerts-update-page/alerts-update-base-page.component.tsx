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
import { AxiosError } from "axios";
import { differenceBy, isEmpty, isEqual, some, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { AlertUpdateResetModal } from "../../components/alert-update-reset-modal/alert-update-reset-modal.component";
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
import { useGetAlert, useResetAlert } from "../../rest/alerts/alerts.actions";
import { updateAlert } from "../../rest/alerts/alerts.rest";
import { Alert, EditableAlert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { useGetSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.actions";
import {
    createSubscriptionGroup,
    updateSubscriptionGroups,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getAlertsAlertPath } from "../../utils/routes/routes.util";
import {
    createEmptySubscriptionGroup,
    getSubscriptionGroupAlertsList,
} from "../../utils/subscription-groups/subscription-groups.util";
import { AlertsEditCreateBasePageComponent } from "../alerts-edit-create-common/alerts-edit-create-base-page.component";
import { QUERY_PARAM_KEY_ANOMALIES_RETRY } from "../alerts-view-page/alerts-view-page.utils";
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
    const { resetAlert } = useResetAlert();

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
    const [isEditRequestInFlight, setIsEditRequestInFlight] = useState(false);
    const [isResetModalOpen, setIsResetModalOpen] = useState(false);

    const [singleNewSubscriptionGroup, setSingleNewSubscriptionGroup] =
        useState<SubscriptionGroup>(createEmptySubscriptionGroup());

    /**
     * This is a placeholder for the state in between the user clicking update alert on the page
     * to the user clicking update alert in the modal
     */
    const [modifiedAlertForUpdate, setModifiedAlertForUpdate] =
        useState<EditableAlert>();

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
                            const alerts =
                                getSubscriptionGroupAlertsList(subGroup);

                            return some(
                                alerts.map(
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
                  notify(NotificationTypeV1.Error, err.message, err.details)
              );
    }, [getAlertErrorMessages, getAlertStatus]);

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
                  notify(NotificationTypeV1.Error, err.message, err.details)
              );
    }, [getSubscriptionGroupsErrorMessages, getSubscriptionGroupStatus]);

    const handleUpdatingSubscriptionGroups = async (
        alert: Alert,
        alertWasReset: boolean
    ): Promise<void> => {
        let copiedCurrentlySelectedSubscriptionGroups = [
            ...currentlySelectedSubscriptionGroups,
        ];
        const searchParams = new URLSearchParams();
        if (alertWasReset) {
            searchParams.set(QUERY_PARAM_KEY_ANOMALIES_RETRY, "true");
        }
        const redirectURL = getAlertsAlertPath(alert.id, searchParams);

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
                    getErrorMessages(error as AxiosError),
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
            navigate(redirectURL);

            return;
        }

        // Add alert to subscription groups
        const subscriptionGroupsToBeAdded = subscriptionGroupsAdded.map(
            (subscriptionGroup) => ({
                ...subscriptionGroup,
                alertAssociations: subscriptionGroup.alertAssociations
                    ? [
                          ...subscriptionGroup.alertAssociations,
                          { alert: { id: alert.id } },
                      ] // Add to existing list
                    : [{ alert: { id: alert.id } }], // Create new list
            })
        );

        // Remove alert from subscription groups
        const subscriptionGroupsToBeOmitted = subscriptionGroupsRemoved.map(
            (subscriptionGroup) => ({
                ...subscriptionGroup,
                alertAssociations: subscriptionGroup.alertAssociations?.filter(
                    (association) => {
                        return association.alert?.id !== alert.id;
                    }
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
                getErrorMessages(error as AxiosError),
                notify,
                t("message.update-error", {
                    entity: t("label.subscription-groups"),
                })
            );
        } finally {
            // Redirect to alerts detail path
            navigate(redirectURL);
        }
    };

    const handleUpdateAlertModalClick = (
        modifiedAlert: EditableAlert,
        shouldDeleteAnomalies: boolean
    ): void => {
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
                if (shouldDeleteAnomalies) {
                    resetAlert(alert.id).finally(() => {
                        handleUpdatingSubscriptionGroups(alert, true);
                    });
                } else {
                    handleUpdatingSubscriptionGroups(alert, false);
                }
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

    const handlePageUpdateAlertClick = (modifiedAlert: EditableAlert): void => {
        const shouldAskForReset =
            !isEqual(
                modifiedAlert.templateProperties,
                originalAlert?.templateProperties
            ) || !isEqual(modifiedAlert?.template, originalAlert?.template);

        if (shouldAskForReset) {
            setModifiedAlertForUpdate(modifiedAlert);
            setIsResetModalOpen(true);
        } else {
            handleUpdateAlertModalClick(modifiedAlert, false);
        }
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
            isError={getAlertStatus === ActionStatus.Error}
            isLoading={loading}
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
                onSubmit={handlePageUpdateAlertClick}
                onSubscriptionGroupChange={
                    setCurrentlySelectedSubscriptionGroups
                }
            />
            {isResetModalOpen && originalAlert && (
                <AlertUpdateResetModal
                    onUpdateAlertClick={(shouldDeleteAnomalies) => {
                        modifiedAlertForUpdate &&
                            handleUpdateAlertModalClick(
                                modifiedAlertForUpdate,
                                shouldDeleteAnomalies
                            );
                    }}
                />
            )}
        </LoadingErrorStateSwitch>
    );
};
