import { Grid } from "@material-ui/core";
import { cloneDeep, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { SubscriptionGroupCard } from "../../components/entity-cards/subscription-group-card/subscription-group-card.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { UiSubscriptionGroupAlertsAccordian } from "../../components/subscription-group-alerts-accordian/subscription-group-alerts-accordian.component";
import { SubscriptionGroupEmailsAccordian } from "../../components/subscription-group-emails-accordian/subscription-group-emails-accordian.component";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    EmailScheme,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import {
    UiSubscriptionGroup,
    UiSubscriptionGroupAlert,
} from "../../rest/dto/ui-subscription-group.interfaces";
import {
    deleteSubscriptionGroup,
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { isValidNumberId } from "../../utils/params/params.util";
import { getSubscriptionGroupsAllPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { getUiSubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";
import { SubscriptionGroupsDetailPageParams } from "./subscription-groups-detail-page.interfaces";

export const SubscriptionGroupsDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [
        uiSubscriptionGroup,
        setUiSubscriptionGroup,
    ] = useState<UiSubscriptionGroup>();
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<SubscriptionGroupsDetailPageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchSubscriptionGroup();
    }, []);

    const onDeleteSubscriptionGroup = (
        uiSubscriptionGroup: UiSubscriptionGroup
    ): void => {
        if (!uiSubscriptionGroup) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: uiSubscriptionGroup.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteSubscriptionGroupConfirmation(uiSubscriptionGroup);
            },
        });
    };

    const onDeleteSubscriptionGroupConfirmation = (
        uiSubscriptionGroup: UiSubscriptionGroup
    ): void => {
        if (!uiSubscriptionGroup) {
            return;
        }

        deleteSubscriptionGroup(uiSubscriptionGroup.id)
            .then((): void => {
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.subscription-group"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Redirect to subscription groups all path
                history.push(getSubscriptionGroupsAllPath());
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", {
                        entity: t("label.subscription-group"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const onUiSubscriptionGroupAlertsChange = (
        uiSubscriptionGroupAlerts: UiSubscriptionGroupAlert[]
    ): void => {
        if (!uiSubscriptionGroup || !uiSubscriptionGroup.subscriptionGroup) {
            return;
        }

        // Create a copy of subscription group and update alerts
        const subscriptionGroupCopy = cloneDeep(
            uiSubscriptionGroup.subscriptionGroup
        );
        subscriptionGroupCopy.alerts = uiSubscriptionGroupAlerts as Alert[];
        saveUpdatedSubscriptionGroup(subscriptionGroupCopy);
    };

    const onSubscriptionGroupEmailsChange = (emails: string[]): void => {
        if (!uiSubscriptionGroup || !uiSubscriptionGroup.subscriptionGroup) {
            return;
        }

        // Create a copy of subscription group and update emails
        const subscriptionGroupCopy = cloneDeep(
            uiSubscriptionGroup.subscriptionGroup
        );
        if (
            subscriptionGroupCopy.notificationSchemes &&
            subscriptionGroupCopy.notificationSchemes.email
        ) {
            subscriptionGroupCopy.notificationSchemes.email.to = emails;
        } else {
            subscriptionGroupCopy.notificationSchemes = {
                email: {
                    to: emails,
                } as EmailScheme,
            };
        }
        saveUpdatedSubscriptionGroup(subscriptionGroupCopy);
    };

    const fetchSubscriptionGroup = (): void => {
        // Validate id from URL
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );
            setLoading(false);

            return;
        }

        Promise.allSettled([
            getSubscriptionGroup(toNumber(params.id)),
            getAllAlerts(),
        ])
            .then(([subscriptionGroupResponse, alertsResponse]): void => {
                // Determine if any of the calls failed
                if (
                    subscriptionGroupResponse.status === "rejected" ||
                    alertsResponse.status === "rejected"
                ) {
                    enqueueSnackbar(
                        t("message.fetch-error"),
                        getErrorSnackbarOption()
                    );
                }

                // Attempt to gather data
                let fetchedAlerts: Alert[] = [];
                if (alertsResponse.status === "fulfilled") {
                    fetchedAlerts = alertsResponse.value;
                    setAlerts(fetchedAlerts);
                }
                if (subscriptionGroupResponse.status === "fulfilled") {
                    setUiSubscriptionGroup(
                        getUiSubscriptionGroup(
                            subscriptionGroupResponse.value,
                            fetchedAlerts
                        )
                    );
                }
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    const saveUpdatedSubscriptionGroup = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        updateSubscriptionGroup(subscriptionGroup)
            .then((subscriptionGroup: SubscriptionGroup): void => {
                enqueueSnackbar(
                    t("message.update-success", {
                        entity: t("label.subscription-group"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Replace updated subscription group as fetched subscription group
                setUiSubscriptionGroup(
                    getUiSubscriptionGroup(subscriptionGroup, alerts)
                );
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", {
                        entity: t("label.subscription-group"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents
            centered
            hideTimeRange
            title={uiSubscriptionGroup ? uiSubscriptionGroup.name : ""}
        >
            {uiSubscriptionGroup && (
                <Grid container>
                    {/* Subscription Group */}
                    <Grid item sm={12}>
                        <SubscriptionGroupCard
                            uiSubscriptionGroup={uiSubscriptionGroup}
                            onDelete={onDeleteSubscriptionGroup}
                        />
                    </Grid>

                    {/* Subscribed alerts */}
                    <Grid item sm={12}>
                        <UiSubscriptionGroupAlertsAccordian
                            alerts={alerts}
                            title={t("label.subscribe-alerts")}
                            uiSubscriptionGroup={uiSubscriptionGroup}
                            onChange={onUiSubscriptionGroupAlertsChange}
                        />
                    </Grid>

                    {/* Subscribed emails */}
                    <Grid item sm={12}>
                        <SubscriptionGroupEmailsAccordian
                            title={t("label.subscribe-emails")}
                            uiSubscriptionGroup={uiSubscriptionGroup}
                            onChange={onSubscriptionGroupEmailsChange}
                        />
                    </Grid>
                </Grid>
            )}

            {/* No data available message */}
            {!uiSubscriptionGroup && <NoDataIndicator />}
        </PageContents>
    );
};
