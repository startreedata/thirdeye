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
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { SubscriptionGroupWizardNew } from "../../components/subscription-group-wizard-new/subscription-group-wizard-new.component";
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
import { createSubscriptionGroup } from "../../rest/subscription-groups/subscription-groups.rest";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsViewPath,
} from "../../utils/routes/routes.util";
import { createEmptySubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";

export const SubscriptionGroupsCreatePage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const { alerts, getAlerts, status: alertsStatus } = useGetAlerts();
    const {
        enumerationItems,
        getEnumerationItems,
        status: enumerationItemsStatus,
    } = useGetEnumerationItems();

    useEffect(() => {
        // Fetching all alerts and enumeration items since this is an edit flow and
        // the new values will need the corresponding entities to be displayed
        getAlerts();
        getEnumerationItems();
    }, []);

    const onSubscriptionGroupWizardFinish = (
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

    const handleOnCancelClick = (): void => {
        navigate(getSubscriptionGroupsAllPath());
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
                {alerts && enumerationItems ? (
                    <SubscriptionGroupWizardNew
                        alerts={alerts}
                        enumerationItems={enumerationItems}
                        submitBtnLabel={t("label.create-entity", {
                            entity: t("label.subscription-group"),
                        })}
                        subscriptionGroup={createEmptySubscriptionGroup()}
                        onCancel={handleOnCancelClick}
                        onFinish={onSubscriptionGroupWizardFinish}
                    />
                ) : (
                    <PageContentsGridV1>
                        <Grid item xs={12}>
                            <NoDataIndicator />
                        </Grid>
                    </PageContentsGridV1>
                )}
            </LoadingErrorStateSwitch>
        </PageV1>
    );
};
