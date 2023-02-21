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
import { Button, Card, CardContent, CardHeader, Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { PageHeader } from "../../components/page-header/page-header.component";
import { PageHeaderProps } from "../../components/page-header/page-header.interfaces";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AlertAssociationsViewTable } from "../../components/subscription-group-view/alert-associations-view-table/alert-associations-view-table.component";
import { NotificationChannelsCard } from "../../components/subscription-group-view/notification-channels-card/notification-channels-card.component";
import { SubscriptionGroupViewCard } from "../../components/subscription-group-view/subscription-group-view-card/subscription-group-view-card.component";
import {
    LocalThemeProviderV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageV1,
    SkeletonV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { lightV1 } from "../../platform/utils";
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
    getSubscriptionGroupsUpdatePath,
    getSubscriptionGroupsViewPath,
} from "../../utils/routes/routes.util";
import { getUiSubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";
import { SubscriptionGroupsViewPageParams } from "./subscription-groups-view-page.interfaces";

export const SubscriptionGroupsViewPage: FunctionComponent = () => {
    const [uiSubscriptionGroup, setUiSubscriptionGroup] =
        useState<UiSubscriptionGroup | null>(null);
    const [status, setStatus] = useState<ActionStatus>(ActionStatus.Initial);

    const { showDialog } = useDialogProviderV1();
    const params = useParams<SubscriptionGroupsViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch subscription group
        fetchSubscriptionGroup();
    }, []);

    const fetchSubscriptionGroup = async (): Promise<void> => {
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
                    subscriptionGroupResponse.reason) ||
                ({} as AxiosError);

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

        fetchedAlerts = alertsResponse.value;

        const enumerationIds =
            (subscriptionGroupResponse.value.alertAssociations
                ?.map((a) => a?.enumerationItem?.id)
                .filter(Boolean) || []) as number[];

        if (enumerationIds && enumerationIds.length > 0) {
            try {
                const enumerationItemsProp = await getEnumerationItems({
                    ids: enumerationIds,
                });

                fetchedUiSubscriptionGroup = getUiSubscriptionGroup(
                    subscriptionGroupResponse.value,
                    fetchedAlerts,
                    enumerationItemsProp || []
                );

                setUiSubscriptionGroup(fetchedUiSubscriptionGroup);
                setStatus(ActionStatus.Done);
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
            }
        } else {
            setUiSubscriptionGroup(fetchedUiSubscriptionGroup);
            setStatus(ActionStatus.Done);
        }
    };

    const handleSubscriptionGroupEdit = (): void => {
        if (!uiSubscriptionGroup) {
            return;
        }
        navigate(getSubscriptionGroupsUpdatePath(uiSubscriptionGroup.id));
    };

    const handleSubscriptionGroupDelete = (): void => {
        if (!uiSubscriptionGroup) {
            return;
        }
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
        customActions: (
            <PageHeaderActionsV1>
                <Button
                    color="primary"
                    variant="outlined"
                    onClick={handleSubscriptionGroupEdit}
                >
                    {t("label.edit")}
                </Button>
                <LocalThemeProviderV1 primary={lightV1.palette.error}>
                    <Button
                        color="primary"
                        variant="outlined"
                        onClick={handleSubscriptionGroupDelete}
                    >
                        {t("label.delete")}
                    </Button>
                </LocalThemeProviderV1>
            </PageHeaderActionsV1>
        ),
    };

    return (
        <PageV1>
            <LoadingErrorStateSwitch
                isError={status === ActionStatus.Error}
                isLoading={status === ActionStatus.Working}
                loadingState={
                    <>
                        <PageContentsGridV1>
                            <Grid item xs={12}>
                                <SkeletonV1 height={56} width={300} />
                                <SkeletonV1 height={80} width={200} />
                                <SkeletonV1 height={180} />
                                <SkeletonV1 height={300} />
                                <SkeletonV1 height={120} />
                                <SkeletonV1 height={120} />
                            </Grid>
                        </PageContentsGridV1>
                    </>
                }
            >
                <PageHeader {...pageHeaderProps} />
                {uiSubscriptionGroup ? (
                    <PageContentsGridV1>
                        <Grid item xs={12}>
                            <SubscriptionGroupViewCard
                                header="Schedule"
                                rows={[
                                    {
                                        label: t("label.name"),
                                        value: uiSubscriptionGroup.name,
                                    },
                                    {
                                        label: t("label.repeat-every"),
                                        value: uiSubscriptionGroup.cron,
                                    },
                                ]}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <Card>
                                <CardHeader title="Active alerts & dimensions" />
                                <CardContent>
                                    <AlertAssociationsViewTable
                                        uiSubscriptionGroup={
                                            uiSubscriptionGroup
                                        }
                                    />
                                </CardContent>
                            </Card>
                        </Grid>
                        <NotificationChannelsCard
                            activeChannels={uiSubscriptionGroup.activeChannels}
                        />

                        {/* TODO: Delete the code and components below */}
                        {/* <Grid item xs={12}>
                            <pre>
                                {JSON.stringify(
                                    uiSubscriptionGroup,
                                    undefined,
                                    4
                                )}
                            </pre>
                        </Grid> */}

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
