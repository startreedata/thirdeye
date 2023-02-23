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
import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { PageHeaderProps } from "../../components/page-header/page-header.interfaces";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { SubscriptionGroupWizard } from "../../components/subscription-group-wizard/subscription-group-wizard.component";
import { SubscriptionGroupViewTabs } from "../../components/subscription-group-wizard/subscription-group-wizard.interfaces";
import { SelectedTab } from "../../components/subscription-group-wizard/subscription-group-wizard.utils";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { useGetEnumerationItems } from "../../rest/enumeration-items/enumeration-items.actions";
import {
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getConfigurationPath,
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsUpdatePath,
    getSubscriptionGroupsViewPath,
} from "../../utils/routes/routes.util";
import { SubscriptionGroupsUpdatePageParams } from "./subscription-groups-update-page.interfaces";

export const SubscriptionGroupsUpdatePage: FunctionComponent = () => {
    const [subscriptionGroupStatus, setSubscriptionGroupStatus] =
        useState<ActionStatus>(ActionStatus.Initial);
    const [subscriptionGroup, setSubscriptionGroup] =
        useState<SubscriptionGroup>();

    const { alerts, getAlerts, status: alertsStatus } = useGetAlerts();
    const {
        enumerationItems,
        getEnumerationItems,
        status: enumerationItemsStatus,
    } = useGetEnumerationItems();

    const params = useParams<SubscriptionGroupsUpdatePageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const [searchParams] = useSearchParams();
    const [selectedTab] = useMemo(
        () => [
            Number(searchParams.get(SelectedTab)) ||
                SubscriptionGroupViewTabs.GroupDetails,
        ],
        [searchParams]
    );

    useEffect(() => {
        fetchSubscriptionGroup();
        // Fetching all alerts and enumeration items since this is an edit flow and
        // the new values will need the corresponding entities to be displayed
        getAlerts();
        getEnumerationItems();
    }, []);

    const id = Number(params.id);

    const pagePath = getSubscriptionGroupsUpdatePath(Number(params.id));

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
            {
                label: id,
                link: pagePath,
            },
        ],
        transparentBackground: true,
        title: t(`label.update-entity`, {
            entity: t("label.subscription-group"),
        }),
        subNavigation: [
            {
                label: t("label.group-details"),
                link: `${pagePath}?${SelectedTab}=${SubscriptionGroupViewTabs.GroupDetails}`,
            },
            {
                label: t("label.alerts-and-dimensions"),
                link: `${pagePath}?${SelectedTab}=${SubscriptionGroupViewTabs.AlertDimensions}`,
            },
        ],
        subNavigationSelected: selectedTab,
    };

    const onSubscriptionGroupWizardFinish = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        updateSubscriptionGroup(subscriptionGroup)
            .then((subscriptionGroup: SubscriptionGroup): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.subscription-group"),
                    })
                );

                // Redirect to subscription groups detail path
                navigate(getSubscriptionGroupsViewPath(subscriptionGroup.id));
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.update-error", {
                        entity: t("label.subscription-group"),
                    })
                );
            });
    };

    const fetchSubscriptionGroup = (): void => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                })
            );
            setSubscriptionGroupStatus(ActionStatus.Error);

            return;
        }

        getSubscriptionGroup(toNumber(params.id))
            .then((data) => {
                setSubscriptionGroup(data);
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

    const handleOnCancelClick = (): void => {
        navigate(getSubscriptionGroupsViewPath(toNumber(params.id)));
    };

    const statusList = [
        alertsStatus,
        enumerationItemsStatus,
        subscriptionGroupStatus,
    ];

    const isLoading = statusList.some((v) => v === ActionStatus.Working);
    const isError = statusList.some((v) => v === ActionStatus.Error);

    const isDataLoaded = !!(subscriptionGroup && alerts && enumerationItems);

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
                    isEmpty={!isDataLoaded}
                >
                    <PageHeader {...pageHeaderProps} />
                    {isDataLoaded ? (
                        <SubscriptionGroupWizard
                            alerts={alerts}
                            cancelBtnLabel={t("label.cancel")}
                            enumerationItems={enumerationItems}
                            selectedTab={selectedTab}
                            submitBtnLabel={t("label.update")}
                            subscriptionGroup={subscriptionGroup}
                            onCancel={handleOnCancelClick}
                            onFinish={onSubscriptionGroupWizardFinish}
                        />
                    ) : null}
                </EmptyStateSwitch>
            </LoadingErrorStateSwitch>
        </PageV1>
    );
};
