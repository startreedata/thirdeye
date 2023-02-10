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
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
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
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { EnumerationItem } from "../../rest/dto/enumeration-item.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { getEnumerationItems } from "../../rest/enumeration-items/enumeration-items.rest";
import {
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { PROMISES } from "../../utils/constants/constants.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsViewPath,
} from "../../utils/routes/routes.util";
import { SubscriptionGroupsUpdatePageParams } from "./subscription-groups-update-page.interfaces";

export const SubscriptionGroupsUpdatePage: FunctionComponent = () => {
    const [status, setStatus] = useState<ActionStatus>(ActionStatus.Initial);
    const [subscriptionGroup, setSubscriptionGroup] =
        useState<SubscriptionGroup>();
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [enumerationItems, setEnumerationItems] = useState<EnumerationItem[]>(
        []
    );
    const params = useParams<SubscriptionGroupsUpdatePageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        fetchSubscriptionGroup();
    }, []);

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

    const fetchSubscriptionGroup = async (): Promise<void> => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                })
            );
            setStatus(ActionStatus.Error);

            return;
        }

        const [subscriptionGroupResponse, alertsResponse] =
            await Promise.allSettled([
                getSubscriptionGroup(toNumber(params.id)),
                getAllAlerts(),
            ]);

        if (
            subscriptionGroupResponse.status === PROMISES.REJECTED ||
            alertsResponse.status === PROMISES.REJECTED
        ) {
            const axiosError: AxiosError =
                (alertsResponse.status === PROMISES.REJECTED &&
                    alertsResponse.reason) ||
                (subscriptionGroupResponse.status === PROMISES.REJECTED &&
                    subscriptionGroupResponse.reason);

            setStatus(ActionStatus.Error);

            notifyIfErrors(
                ActionStatus.Error,
                getErrorMessages(axiosError),
                notify,
                t("message.error-while-fetching", {
                    entity: t(
                        alertsResponse.status === PROMISES.REJECTED
                            ? "label.alerts"
                            : "label.subscription-group"
                    ),
                })
            );

            return;
        }

        setAlerts(alertsResponse.value);
        setSubscriptionGroup(subscriptionGroupResponse.value);

        const enumerationIds =
            (subscriptionGroupResponse.value.alertAssociations
                ?.map((a) => a?.enumerationItem?.id)
                .filter(Boolean) || []) as number[];

        if (enumerationIds && enumerationIds.length > 0) {
            let enumerationItems: EnumerationItem[] | null = null;
            try {
                enumerationItems = await getEnumerationItems({
                    ids: enumerationIds,
                });
                setEnumerationItems(enumerationItems);
            } catch (err) {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(err as AxiosError),
                    notify,
                    t("message.error-while-fetching", {
                        entity: t("label.enumeration-item"),
                    })
                );

                setStatus(ActionStatus.Error);

                return;
            }
        }

        setStatus(ActionStatus.Done);
    };

    const handleOnCancelClick = (): void => {
        navigate(getSubscriptionGroupsAllPath());
    };

    return (
        <PageV1>
            <LoadingErrorStateSwitch
                isError={status === ActionStatus.Error}
                isLoading={status === ActionStatus.Working}
                loadingState={<AppLoadingIndicatorV1 />}
            >
                {subscriptionGroup ? (
                    <SubscriptionGroupWizardNew
                        isExisting
                        alerts={alerts}
                        enumerationItems={enumerationItems}
                        submitBtnLabel={t("label.update-entity", {
                            entity: t("label.subscription-group"),
                        })}
                        subscriptionGroup={subscriptionGroup}
                        onCancel={handleOnCancelClick}
                        onFinish={onSubscriptionGroupWizardFinish}
                    />
                ) : (
                    // <SubscriptionGroupWizard
                    //     alerts={alerts}
                    //     submitBtnLabel={t("label.update-entity", {
                    //         entity: t("label.subscription-group"),
                    //     })}
                    //     subscriptionGroup={subscriptionGroup}
                    //     onCancel={handleOnCancelClick}
                    //     onFinish={onSubscriptionGroupWizardFinish}
                    // />
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
