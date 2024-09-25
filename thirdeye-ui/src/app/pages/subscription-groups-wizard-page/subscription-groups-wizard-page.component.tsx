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
import { Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useLocation } from "react-router-dom";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { PageHeaderProps } from "../../components/page-header/page-header.interfaces";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { Association } from "../../components/subscription-group-wizard/subscription-group-wizard.interfaces";
import { getAssociations } from "../../components/subscription-group-wizard/subscription-group-wizard.utils";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import {
    AlertAssociation,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import { useGetEnumerationItems } from "../../rest/enumeration-items/enumeration-items.actions";
import { getSubscriptionGroup } from "../../rest/subscription-groups/subscription-groups.rest";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    AppRouteRelative,
    getConfigurationPath,
    getSubscriptionGroupsAllPath,
} from "../../utils/routes/routes.util";
import { createEmptySubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";
import { SubscriptionGroupsWizardPageProps } from "./subscription-groups-wizard-page.interfaces";

export const SubscriptionGroupsWizardPage: FunctionComponent<SubscriptionGroupsWizardPageProps> =
    ({
        subscriptionGroupId,
        pageHeaderTitle,
        pageHeaderActionCrumb,
        submitButtonLabel,
        onCancel,
        onFinish,
    }) => {
        const { t } = useTranslation();
        const { notify } = useNotificationProviderV1();

        const { alerts, getAlerts, status: alertsStatus } = useGetAlerts();
        const {
            enumerationItems,
            getEnumerationItems,
            status: enumerationItemsStatus,
        } = useGetEnumerationItems();

        const [subscriptionGroup, setSubscriptionGroup] =
            useState<SubscriptionGroup>(createEmptySubscriptionGroup());
        const [subscriptionGroupStatus, setSubscriptionGroupStatus] =
            useState<ActionStatus>(ActionStatus.Working);
        const [editedAssociations, setEditedAssociations] = useState<
            Association[]
        >([]);

        useEffect(() => {
            fetchSubscriptionGroup(subscriptionGroupId);
            // Fetching all alerts and enumeration items since this is an edit flow and
            // the new values will need the corresponding entities to be displayed
            getAlerts();
            getEnumerationItems();
        }, []);

        useEffect(() => {
            const newSubscriptionGroupAssociations: AlertAssociation[] =
                editedAssociations.map(({ alertId, enumerationId }) => ({
                    alert: { id: alertId },
                    ...(enumerationId && {
                        enumerationItem: { id: enumerationId },
                    }),
                }));

            // Remove the @deprecated `alerts` key from existing data
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            setSubscriptionGroup(({ alerts, ...stateProp }) => ({
                ...stateProp,
                alertAssociations: newSubscriptionGroupAssociations,
            }));
        }, [editedAssociations]);

        const fetchSubscriptionGroup = (id?: string): void => {
            if (!id) {
                setSubscriptionGroupStatus(ActionStatus.Done);

                return;
            }

            // Validate id from URL
            if (id && !isValidNumberId(id)) {
                notify(
                    NotificationTypeV1.Error,
                    t("message.invalid-id", {
                        entity: t("label.subscription-group"),
                        id,
                    })
                );
                setSubscriptionGroupStatus(ActionStatus.Error);

                return;
            }

            getSubscriptionGroup(toNumber(id))
                .then((data) => {
                    setSubscriptionGroup(data);
                    setSubscriptionGroupStatus(ActionStatus.Done);
                    setEditedAssociations(getAssociations(data));
                })
                .catch((err) => {
                    notifyIfErrors(
                        ActionStatus.Error,
                        getErrorMessages(err),
                        notify,
                        t("message.error-while-fetching", {
                            entity: t("label.subscription-group"),
                        })
                    );
                });
        };

        const { pathname } = useLocation();

        const pageHeaderProps: PageHeaderProps = {
            breadcrumbs: [
                {
                    label: t("label.configuration"),
                    link: getConfigurationPath(),
                },
                {
                    label: t("label.subscription-groups"),
                    link: getSubscriptionGroupsAllPath(),
                },
                pageHeaderActionCrumb,
            ],
            title: pageHeaderTitle,
            subNavigation: [
                {
                    label: t("label.group-details"),
                    link: `../${AppRouteRelative.SUBSCRIPTION_GROUPS_WIZARD_DETAILS}`,
                },
                {
                    label: t("label.alerts-and-dimensions"),
                    link: `../${AppRouteRelative.SUBSCRIPTION_GROUPS_WIZARD_ALERT_DIMENSIONS}`,
                },
            ],
            subNavigationSelected: pathname.includes(
                AppRouteRelative.SUBSCRIPTION_GROUPS_WIZARD_DETAILS
            )
                ? 0
                : 1,
        };

        const statusList = [
            subscriptionGroupStatus,
            alertsStatus,
            enumerationItemsStatus,
        ];

        const isLoading = statusList.some((v) => v === ActionStatus.Working);

        const isError = statusList.some((v) => v === ActionStatus.Error);

        return (
            <PageV1>
                <LoadingErrorStateSwitch
                    isError={isError}
                    isLoading={isLoading}
                    loadingState={<AppLoadingIndicatorV1 />}
                >
                    <EmptyStateSwitch
                        emptyState={
                            <PageContentsGridV1>
                                <Grid item xs={12}>
                                    <NoDataIndicator />
                                </Grid>
                            </PageContentsGridV1>
                        }
                        isEmpty={
                            !(alerts && enumerationItems && subscriptionGroup)
                        }
                    >
                        <PageHeader {...pageHeaderProps} />
                        <Outlet
                            context={{
                                alerts,
                                enumerationItems,
                                subscriptionGroup,
                                setSubscriptionGroup,
                                associations: editedAssociations,
                                setAssociations: setEditedAssociations,
                                submitButtonLabel,
                                onCancel,
                                onFinish,
                            }}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageV1>
        );
    };
