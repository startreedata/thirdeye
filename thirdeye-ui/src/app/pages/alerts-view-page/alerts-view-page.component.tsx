import { Box, Button, Grid } from "@material-ui/core";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import {
    AppLoadingIndicatorV1,
    DropdownButtonTypeV1,
    DropdownButtonV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageV1,
} from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AlertCardV1 } from "../../components/entity-cards/alert-card-v1/alert-card-v1.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import {
    deleteAlert,
    getAlert,
    updateAlert,
} from "../../rest/alerts/alerts.rest";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.rest";
import {
    createAlertEvaluation,
    getUiAlert,
} from "../../utils/alerts/alerts.util";
import { isValidNumberId } from "../../utils/params/params.util";
import {
    getAlertsAllPath,
    getAlertsUpdatePath,
} from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { AlertsViewPageParams } from "./alerts-view-page.interfaces";

export const AlertsViewPage: FunctionComponent = () => {
    const { evaluation, getEvaluation } = useGetEvaluation();
    const [uiAlert, setUiAlert] = useState<UiAlert | null>(null);
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const [
        alertEvaluation,
        setAlertEvaluation,
    ] = useState<AlertEvaluation | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<AlertsViewPageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch alert
        fetchAlert();
    }, [timeRangeDuration]);

    useEffect(() => {
        !!evaluation && setAlertEvaluation(evaluation);
    }, [evaluation]);

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [uiAlert]);

    const fetchAlertEvaluation = (): void => {
        if (!uiAlert || !uiAlert.alert) {
            setAlertEvaluation(null);

            return;
        }
        getEvaluation(
            createAlertEvaluation(
                uiAlert.alert,
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            )
        );
    };

    const fetchAlert = (): void => {
        setUiAlert(null);
        let fetchedUiAlert = {} as UiAlert;
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];

        if (!isValidNumberId(params.id)) {
            // Invalid id
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );

            setUiAlert(fetchedUiAlert);
            setSubscriptionGroups(fetchedSubscriptionGroups);

            return;
        }

        Promise.allSettled([
            getAlert(toNumber(params.id)),
            getAllSubscriptionGroups(),
        ])
            .then(([alertResponse, subscriptionGroupsResponse]) => {
                // Attempt to gather data
                if (subscriptionGroupsResponse.status === "fulfilled") {
                    fetchedSubscriptionGroups =
                        subscriptionGroupsResponse.value;
                }
                if (alertResponse.status === "fulfilled") {
                    fetchedUiAlert = getUiAlert(
                        alertResponse.value,
                        fetchedSubscriptionGroups
                    );
                }
            })
            .finally(() => {
                setUiAlert(fetchedUiAlert);
                setSubscriptionGroups(fetchedSubscriptionGroups);
            });
    };

    const handleAlertChange = (uiAlert: UiAlert): void => {
        if (!uiAlert.alert) {
            return;
        }

        updateAlert(uiAlert.alert).then((alert) => {
            enqueueSnackbar(
                t("message.update-success", { entity: t("label.alert") }),
                getSuccessSnackbarOption()
            );

            // Replace updated alert as fetched alert
            setUiAlert(getUiAlert(alert, subscriptionGroups));
        });
    };

    const handleAlertDelete = (): void => {
        if (!uiAlert) {
            return;
        }
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiAlert.name }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleAlertDeleteOk(uiAlert),
        });
    };

    const handleAlertDeleteOk = (uiAlert: UiAlert): void => {
        deleteAlert(uiAlert.id).then(() => {
            enqueueSnackbar(
                t("message.delete-success", { entity: t("label.alert") }),
                getSuccessSnackbarOption()
            );

            // Redirect to alerts all path
            history.push(getAlertsAllPath());
        });
    };

    const handleAlertStateToggle = (): void => {
        if (uiAlert && uiAlert.alert) {
            uiAlert.alert.active = !uiAlert.alert.active;
            handleAlertChange(uiAlert);
        }
    };

    const handleAlertEdit = (): void => {
        if (uiAlert) {
            history.push(getAlertsUpdatePath(uiAlert.id));
        }
    };

    const handleAlertMenuOnclick = (id: number | string, _: string): void => {
        switch (id) {
            case "activateDeactivateAlert":
                handleAlertStateToggle();

                break;
            case "editAlert":
                handleAlertEdit();

                break;
            case "deleteAlert":
                handleAlertDelete();

                break;
            default:
                break;
        }
    };

    const alertMenuItems = [
        {
            id: "activateDeactivateAlert",
            text: uiAlert?.active
                ? t("label.deactivate-entity", {
                      entity: t("label.alert"),
                  })
                : t("label.activate-entity", {
                      entity: t("label.alert"),
                  }),
        },
        {
            id: "editAlert",
            text: t("label.edit-entity", {
                entity: t("label.alert"),
            }),
        },
        {
            id: "deleteAlert",
            text: t("label.delete-entity", {
                entity: t("label.alert"),
            }),
        },
    ];

    return !uiAlert ? (
        <AppLoadingIndicatorV1 />
    ) : (
        <PageV1>
            <PageHeaderV1>
                <PageHeaderTextV1>
                    {uiAlert.name}
                    <Box component="span" paddingLeft={3}>
                        <Button
                            disableRipple
                            startIcon={
                                uiAlert.active ? (
                                    <CheckIcon color="primary" />
                                ) : (
                                    <CloseIcon color="error" />
                                )
                            }
                        >
                            {t("label.active")}
                        </Button>
                    </Box>
                </PageHeaderTextV1>
                <PageHeaderActionsV1>
                    <DropdownButtonV1
                        dropdownMenuItems={alertMenuItems}
                        type={DropdownButtonTypeV1.MoreOptions}
                        onClick={handleAlertMenuOnclick}
                    >
                        {t("label.create")}
                    </DropdownButtonV1>
                </PageHeaderActionsV1>
            </PageHeaderV1>

            <PageContentsGridV1>
                {/* Alert Details Card*/}
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <AlertCardV1 showCreatedBy uiAlert={uiAlert} />
                    </PageContentsCardV1>
                </Grid>

                {/* Alert evaluation time series */}
                <Grid item xs={12}>
                    <AlertEvaluationTimeSeriesCard
                        alertEvaluation={alertEvaluation}
                        alertEvaluationTimeSeriesHeight={300}
                        title={uiAlert.name}
                        onRefresh={fetchAlertEvaluation}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
