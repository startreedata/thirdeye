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
import { Box, Button, Grid } from "@material-ui/core";
import { useQuery } from "@tanstack/react-query";
import { AxiosError } from "axios";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { SubscriptionGroupListV1 } from "../../components/subscription-group-list-v1/subscription-group-list-v1.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";
import { useGetEnumerationItems } from "../../rest/enumeration-items/enumeration-items.actions";
import {
    deleteSubscriptionGroup,
    getAllSubscriptionGroups,
    SUBSCRIPTION_GROUP_CACHE_KEYS,
} from "../../rest/subscription-groups/subscription-groups.rest";
import {
    makeDeleteRequest,
    promptDeleteConfirmation,
} from "../../utils/bulk-delete/bulk-delete.util";
import {
    notifyErrors,
    notifyIfErrors,
} from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getSubscriptionGroupsCreatePath } from "../../utils/routes/routes.util";
import { getUiSubscriptionGroups } from "../../utils/subscription-groups/subscription-groups.util";

export const SubscriptionGroupsAllPage: FunctionComponent = () => {
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const {
        data: subscriptionGroups,
        isError: isSubscriptionGroupsRequestError,
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
        alerts,
        getAlerts,
        status: getAlertsStatus,
        errorMessages: getAlertsErrorMessages,
    } = useGetAlerts();

    const {
        enumerationItems,
        getEnumerationItems,
        status: getEnumerationItemsStatus,
        errorMessages: getEnumerationItemsErrorMessages,
    } = useGetEnumerationItems();

    const [uiSubscriptionGroups, setUiSubscriptionGroups] = useState<
        UiSubscriptionGroup[]
    >([]);

    useEffect(() => {
        // Time range refreshed, fetch subscription groups
        fetchAllSubscriptionGroups();
    }, []);

    useEffect(() => {
        notifyIfErrors(
            getAlertsStatus,
            getAlertsErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alerts"),
            })
        );
    }, [getAlertsStatus]);

    useEffect(() => {
        notifyIfErrors(
            getEnumerationItemsStatus,
            getEnumerationItemsErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.enumeration-items"),
            })
        );
    }, [getEnumerationItemsStatus]);

    useEffect(() => {
        if (!alerts || !subscriptionGroups || !enumerationItems) {
            return;
        }
        setUiSubscriptionGroups(
            getUiSubscriptionGroups(
                subscriptionGroups,
                alerts,
                enumerationItems
            )
        );
    }, [alerts, subscriptionGroups, enumerationItems]);

    const fetchAllSubscriptionGroups = (): void => {
        setUiSubscriptionGroups([]);

        getEnumerationItems();
        getAlerts();
    };

    const handleSubscriptionGroupDelete = (
        uiSubscriptionGroupToDelete: UiSubscriptionGroup[]
    ): void => {
        promptDeleteConfirmation(
            uiSubscriptionGroupToDelete,
            () => {
                uiSubscriptionGroups &&
                    makeDeleteRequest(
                        uiSubscriptionGroupToDelete,
                        deleteSubscriptionGroup,
                        t,
                        notify,
                        t("label.subscription-group"),
                        t("label.subscription-groups")
                    ).then((deleted) => {
                        setUiSubscriptionGroups(() => {
                            return [...uiSubscriptionGroups].filter(
                                (candidate) => {
                                    return (
                                        deleted.findIndex(
                                            (d) => d.id === candidate.id
                                        ) === -1
                                    );
                                }
                            );
                        });
                    });
            },
            t,
            showDialog,
            t("label.subscription-groups")
        );
    };
    const statusList = [
        isSubscriptionGroupsRequestError
            ? ActionStatus.Error
            : ActionStatus.Done,
        getAlertsStatus,
        getEnumerationItemsStatus,
    ];

    const isError = statusList.some((status) => status === ActionStatus.Error);
    const isLoading = statusList.some(
        (status) => status === ActionStatus.Working
    );

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={4} />
            <PageContentsGridV1 fullHeight>
                <LoadingErrorStateSwitch
                    wrapInCard
                    wrapInGrid
                    isError={isError}
                    isLoading={isLoading}
                >
                    <EmptyStateSwitch
                        emptyState={
                            <Grid item xs={12}>
                                <PageContentsCardV1>
                                    <Box padding={20}>
                                        <NoDataIndicator>
                                            <Box textAlign="center">
                                                {t(
                                                    "message.no-entity-created",
                                                    {
                                                        entity: t(
                                                            "label.subscription-groups"
                                                        ),
                                                    }
                                                )}
                                            </Box>
                                            <Box
                                                marginTop={2}
                                                textAlign="center"
                                            >
                                                <Button
                                                    color="primary"
                                                    href={getSubscriptionGroupsCreatePath()}
                                                >
                                                    {t("label.create-entity", {
                                                        entity: t(
                                                            "label.subscription-group"
                                                        ),
                                                    })}
                                                </Button>
                                            </Box>
                                        </NoDataIndicator>
                                    </Box>
                                </PageContentsCardV1>
                            </Grid>
                        }
                        isEmpty={uiSubscriptionGroups.length === 0}
                    >
                        <SubscriptionGroupListV1
                            subscriptionGroups={uiSubscriptionGroups}
                            onDelete={handleSubscriptionGroupDelete}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
