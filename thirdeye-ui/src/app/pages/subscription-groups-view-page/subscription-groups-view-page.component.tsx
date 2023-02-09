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
import { PageHeader } from "../../components/page-header/page-header.component";
import { PageHeaderProps } from "../../components/page-header/page-header.interfaces";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AlertsDimensions } from "../../components/subscription-group-view/alerts-dimensions/alerts-dimentions.component";
import { SubscriptionGroupDetails } from "../../components/subscription-group-view/subscription-group-details/subscription-group-details.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    SkeletonV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";
import { getEnumerationItems } from "../../rest/enumeration-items/enumeration-items.rest";
import {
    deleteSubscriptionGroup,
    getSubscriptionGroup,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { PROMISES } from "../../utils/constants/constants.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getConfigurationPath,
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsViewPath,
} from "../../utils/routes/routes.util";
import { getUiSubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";
import { SubscriptionGroupsViewPageParams } from "./subscription-groups-view-page.interfaces";

enum SubscriptionGroupViewTabs {
    GroupDetails,
    AlertDimensions,
}

const SelectedTab = "selectedTab";

export const SubscriptionGroupsViewPage: FunctionComponent = () => {
    const [uiSubscriptionGroup, setUiSubscriptionGroup] =
        useState<UiSubscriptionGroup | null>(null);
    const [status, setStatus] = useState<ActionStatus>(ActionStatus.Initial);

    const { showDialog } = useDialogProviderV1();
    const params = useParams<SubscriptionGroupsViewPageParams>();
    const [searchParams] = useSearchParams();
    const [selectedTab] = useMemo(
        () => [
            Number(searchParams.get(SelectedTab)) ||
                SubscriptionGroupViewTabs.GroupDetails,
        ],
        [searchParams]
    );
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch subscription group
        fetchSubscriptionGroup();
    }, []);

    const fetchSubscriptionGroup = (): void => {
        setUiSubscriptionGroup(null);
        let fetchedUiSubscriptionGroup = {} as UiSubscriptionGroup;
        let fetchedAlerts: Alert[] = [];

        if (params.id && !isValidNumberId(params.id)) {
            // Invalid id
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                })
            );

            setUiSubscriptionGroup(fetchedUiSubscriptionGroup);

            return;
        }

        setStatus(ActionStatus.Working);
        Promise.allSettled([
            getSubscriptionGroup(toNumber(params.id)),
            getAllAlerts(),
        ])
            .then(([subscriptionGroupResponse, alertsResponse]) => {
                // Determine if any of the calls failed
                setStatus(ActionStatus.Working);
                if (
                    subscriptionGroupResponse.status === PROMISES.REJECTED ||
                    alertsResponse.status === PROMISES.REJECTED
                ) {
                    const axiosError =
                        alertsResponse.status === PROMISES.REJECTED
                            ? alertsResponse.reason
                            : subscriptionGroupResponse.status ===
                              PROMISES.REJECTED
                            ? subscriptionGroupResponse.reason
                            : ({} as AxiosError);

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
                }

                // Attempt to gather data
                if (alertsResponse.status === PROMISES.FULFILLED) {
                    fetchedAlerts = alertsResponse.value;
                }
                if (subscriptionGroupResponse.status === PROMISES.FULFILLED) {
                    const enumerationIds =
                        (subscriptionGroupResponse.value.alertAssociations
                            ?.map((a) => a?.enumerationItem?.id)
                            .filter(Boolean) || []) as number[];

                    // TODO: Only call if needed, maybe convert to async await
                    (enumerationIds && enumerationIds.length > 0
                        ? getEnumerationItems({ ids: enumerationIds })
                        : Promise.resolve()
                    )
                        .then((enumerationItems) => {
                            fetchedUiSubscriptionGroup = getUiSubscriptionGroup(
                                subscriptionGroupResponse.value,
                                fetchedAlerts,
                                enumerationItems || []
                            );

                            setUiSubscriptionGroup(fetchedUiSubscriptionGroup);
                            setStatus(ActionStatus.Done);
                        })
                        .catch((err) => {
                            notifyIfErrors(
                                ActionStatus.Error,
                                getErrorMessages(err),
                                notify,
                                t("message.error-while-fetching", {
                                    entity: t("label.enumeration-item"),
                                })
                            );

                            setStatus(ActionStatus.Error);
                        });
                }
            })
            .catch(() => {
                setStatus(ActionStatus.Error);
            });
    };

    const handleSubscriptionGroupDelete = (
        uiSubscriptionGroup: UiSubscriptionGroup
    ): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: uiSubscriptionGroup.name,
            }),
            cancelButtonText: t("label.cancel"),
            okButtonText: t("label.confirm"),
            onOk: () => handleSubscriptionGroupDeleteOk(uiSubscriptionGroup),
        });
    };

    const handleSubscriptionGroupDeleteOk = (
        uiSubscriptionGroup: UiSubscriptionGroup
    ): void => {
        deleteSubscriptionGroup(uiSubscriptionGroup.id).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", {
                    entity: t("label.subscription-group"),
                })
            );

            // Redirect to subscription groups all path
            navigate(getSubscriptionGroupsAllPath());
        });
    };

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
                label: Number(params.id),
                link: getSubscriptionGroupsViewPath(Number(params.id)),
            },
        ],
        transparentBackground: true,
        title: uiSubscriptionGroup?.name || "",
        subNavigation: [
            {
                label: t("label.group-details"),
                link: `${getSubscriptionGroupsViewPath(
                    Number(params.id)
                )}?selectedTab=${SubscriptionGroupViewTabs.GroupDetails}`,
            },
            {
                label: t("label.alerts-and-dimensions"),
                link: `${getSubscriptionGroupsViewPath(
                    Number(params.id)
                )}?selectedTab=${SubscriptionGroupViewTabs.AlertDimensions}`,
            },
        ],
        subNavigationSelected: selectedTab,
    };

    // const [editedSubscriptionGroup, setEditedSubscriptionGroup] = useState()

    return (
        <PageV1>
            <LoadingErrorStateSwitch
                isError={status === ActionStatus.Error}
                isLoading={status === ActionStatus.Working}
                loadingState={
                    <>
                        <PageHeader>
                            <SkeletonV1 height={48} width={300} />
                        </PageHeader>
                        <PageContentsGridV1>
                            <Grid item xs={12}>
                                <SkeletonV1 height={160} />
                                <SkeletonV1 height={160} />
                            </Grid>
                        </PageContentsGridV1>
                    </>
                }
            >
                <PageHeader {...pageHeaderProps} />
                {uiSubscriptionGroup ? (
                    <PageContentsGridV1>
                        {selectedTab ===
                        SubscriptionGroupViewTabs.GroupDetails ? (
                            <SubscriptionGroupDetails
                                uiSubscriptionGroup={uiSubscriptionGroup}
                            />
                        ) : null}
                        {selectedTab ===
                        SubscriptionGroupViewTabs.AlertDimensions ? (
                            <AlertsDimensions
                                uiSubscriptionGroupAlerts={
                                    uiSubscriptionGroup.alerts
                                }
                            />
                        ) : null}

                        <Grid item xs={12}>
                            <pre>
                                {JSON.stringify(
                                    uiSubscriptionGroup,
                                    undefined,
                                    4
                                )}
                            </pre>
                        </Grid>

                        {/* <SubscriptionGroupCard
                            uiSubscriptionGroup={uiSubscriptionGroup}
                            onDelete={handleSubscriptionGroupDelete}
                        /> */}
                        {/* Notifications Groups */}
                        {/* <Grid item xs={12}>
                        {uiSubscriptionGroup &&
                            uiSubscriptionGroup.subscriptionGroup && (
                                <SubscriptionGroupSpecsCard
                                    specs={
                                        uiSubscriptionGroup.subscriptionGroup
                                            .specs || []
                                    }
                                />
                            )}
                    </Grid> */}
                    </PageContentsGridV1>
                ) : null}
            </LoadingErrorStateSwitch>
        </PageV1>
    );
};
