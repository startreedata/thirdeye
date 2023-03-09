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
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";
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
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import { Alert } from "../../rest/dto/alert.interfaces";
import { EnumerationItem } from "../../rest/dto/enumeration-item.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { useGetEnumerationItems } from "../../rest/enumeration-items/enumeration-items.actions";
import { createSubscriptionGroup } from "../../rest/subscription-groups/subscription-groups.rest";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getConfigurationPath,
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsCreatePath,
    getSubscriptionGroupsViewPath,
} from "../../utils/routes/routes.util";
import { createEmptySubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";

export const SubscriptionGroupsCreatePage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const { showDialog } = useDialogProviderV1();

    const { alerts, getAlerts, status: alertsStatus } = useGetAlerts();
    const {
        enumerationItems,
        getEnumerationItems,
        status: enumerationItemsStatus,
    } = useGetEnumerationItems();

    const [searchParams, setSearchParams] = useSearchParams();
    const [selectedTab] = useMemo(
        () => [
            Number(searchParams.get(SelectedTab)) ||
                SubscriptionGroupViewTabs.GroupDetails,
        ],
        [searchParams]
    );

    useEffect(() => {
        // Fetching all alerts and enumeration items since this is an edit flow and
        // the new values will need the corresponding entities to be displayed
        getAlerts();
        getEnumerationItems();
    }, []);

    const handleSubscriptionGroupWizardFinish = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        createSubscriptionGroup(subscriptionGroup)
            .then((subscriptionGroup: SubscriptionGroup): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", {
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
                    t("message.create-error", {
                        entity: t("label.subscription-group"),
                    })
                );
            });
    };

    const setSelectedTab = (goToTab: SubscriptionGroupViewTabs): void => {
        searchParams.set(SelectedTab, goToTab.toString());
        setSearchParams(searchParams);
    };

    const handleSubscriptionGroupWizardFinishDialog = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t(
                "message.are-you-sure-to-proceed-without-any-alerts-dimensions-added-to-subscription-group"
            ),
            okButtonText: t("label.yes"),
            cancelButtonText: t("label.no"),
            onOk: () => handleSubscriptionGroupWizardFinish(subscriptionGroup),
        });
    };

    const handleOnNextClick = (subscriptionGroup: SubscriptionGroup): void => {
        // If on the last tab
        if (selectedTab === SubscriptionGroupViewTabs.AlertDimensions) {
            if (isEmpty(subscriptionGroup.alertAssociations)) {
                // If there are no alert associations, show a dialog confirming if this is intended
                handleSubscriptionGroupWizardFinishDialog(subscriptionGroup);
            } else {
                // Otherwise, proceed with saving the data
                handleSubscriptionGroupWizardFinish(subscriptionGroup);
            }
        } else {
            // Go to the next tab
            setSelectedTab(SubscriptionGroupViewTabs.AlertDimensions);
        }
    };

    const handleOnCancelClick = (): void => {
        if (selectedTab === SubscriptionGroupViewTabs.AlertDimensions) {
            setSelectedTab(SubscriptionGroupViewTabs.GroupDetails);
        } else {
            navigate(getSubscriptionGroupsAllPath());
        }
    };

    const nextButtonLabel =
        selectedTab === SubscriptionGroupViewTabs.GroupDetails
            ? t("label.next")
            : t("label.save");

    const cancelButtonLabel =
        selectedTab === SubscriptionGroupViewTabs.GroupDetails
            ? t("label.cancel")
            : t("label.back");

    const pagePath = getSubscriptionGroupsCreatePath();

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
                label: t("label.create"),
                link: pagePath,
            },
        ],
        title: t(`label.create-entity`, {
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

    const isLoading = [alertsStatus, enumerationItemsStatus].some(
        (v) => v === ActionStatus.Working
    );

    const isError = [alertsStatus, enumerationItemsStatus].some(
        (v) => v === ActionStatus.Error
    );

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
                    isEmpty={!(alerts && enumerationItems)}
                >
                    <PageHeader {...pageHeaderProps} />
                    <SubscriptionGroupWizard
                        alerts={alerts as Alert[]}
                        cancelBtnLabel={cancelButtonLabel}
                        enumerationItems={enumerationItems as EnumerationItem[]}
                        selectedTab={selectedTab}
                        submitBtnLabel={nextButtonLabel}
                        subscriptionGroup={createEmptySubscriptionGroup()}
                        onCancel={handleOnCancelClick}
                        onFinish={handleOnNextClick}
                    />
                </EmptyStateSwitch>
            </LoadingErrorStateSwitch>
        </PageV1>
    );
};
