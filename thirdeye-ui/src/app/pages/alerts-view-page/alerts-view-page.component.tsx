import { Button, Grid, Menu, MenuItem, Typography } from "@material-ui/core";
import { MoreHoriz } from "@material-ui/icons";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import {
    PageContentsGridV1,
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
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import {
    deleteAlert,
    getAlert,
    getAlertEvaluation,
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
import { theme } from "../../utils/material-ui/theme.util";
import { isValidNumberId } from "../../utils/params/params.util";
import {
    getAlertsAllPath,
    getAlertsUpdatePath,
} from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { useAlertDetailsStyles } from "./alert-view-page.styles";
import { AlertsViewPageParams } from "./alerts-view-page.interfaces";

export const AlertsViewPage: FunctionComponent = () => {
    const [uiAlert, setUiAlert] = useState<UiAlert | null>(null);
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const [
        alertEvaluation,
        setAlertEvaluation,
    ] = useState<AlertEvaluation | null>(null);
    const classes = useAlertDetailsStyles();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<AlertsViewPageParams>();
    const history = useHistory();
    const { t } = useTranslation();
    const [
        alertOptionsAnchorElement,
        setAlertOptionsAnchorElement,
    ] = useState<HTMLElement | null>();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch alert
        fetchAlert();
    }, [timeRangeDuration]);

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [uiAlert]);

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

    const fetchAlertEvaluation = (): void => {
        setAlertEvaluation(null);
        let fetchedAlertEvaluation = {} as AlertEvaluation;

        if (!uiAlert || !uiAlert.alert) {
            setAlertEvaluation(fetchedAlertEvaluation);

            return;
        }

        getAlertEvaluation(
            createAlertEvaluation(
                uiAlert.alert,
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            )
        )
            .then((alertEvaluation) => {
                fetchedAlertEvaluation = alertEvaluation;
            })
            .finally(() => setAlertEvaluation(fetchedAlertEvaluation));
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

    const handleAlertDelete = (uiAlert: UiAlert): void => {
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

    const OptionsDelete = (): void => {
        if (uiAlert) {
            handleAlertDelete(uiAlert);
        }
    };

    const handleAlertOptionsClick = (
        event: React.MouseEvent<HTMLElement>
    ): void => {
        setAlertOptionsAnchorElement(event.currentTarget);
    };

    const handleAlertOptionsClose = (): void => {
        setAlertOptionsAnchorElement(null);
    };

    const handleAlertStateToggle = (): void => {
        if (uiAlert && uiAlert.alert) {
            uiAlert.alert.active = !uiAlert.alert.active;
            handleAlertChange(uiAlert);
            handleAlertOptionsClose();
        }
    };

    const handleAlertEdit = (): void => {
        if (uiAlert) {
            history.push(getAlertsUpdatePath(uiAlert.id));
            handleAlertOptionsClose();
        }
    };

    return !uiAlert ? (
        <LoadingIndicator />
    ) : (
        <PageV1>
            <PageHeaderV1>
                <Grid container alignItems="center" spacing={4}>
                    <Grid item>
                        <PageHeaderTextV1>
                            {uiAlert ? uiAlert.name : ""}
                        </PageHeaderTextV1>
                    </Grid>
                    <Grid item>
                        <Grid container>
                            <div className={classes.active}>
                                <Grid item>
                                    {uiAlert?.active ? (
                                        <CheckIcon
                                            fontSize="small"
                                            htmlColor="#00a3de"
                                        />
                                    ) : (
                                        <CloseIcon
                                            fontSize="small"
                                            htmlColor={theme.palette.error.main}
                                        />
                                    )}
                                </Grid>
                                <Grid item>
                                    <Typography className={classes.smallSize}>
                                        Active
                                    </Typography>
                                </Grid>
                            </div>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item>
                    {/* Alert options button */}
                    <Button
                        className={classes.background}
                        onClick={handleAlertOptionsClick}
                    >
                        <MoreHoriz />
                    </Button>
                    {/* Alert options */}
                    <Menu
                        anchorEl={alertOptionsAnchorElement}
                        open={Boolean(alertOptionsAnchorElement)}
                        onClose={handleAlertOptionsClose}
                    >
                        {/* Activate/deactivete alert */}
                        <MenuItem onClick={handleAlertStateToggle}>
                            {uiAlert?.active
                                ? t("label.deactivate-entity", {
                                      entity: t("label.alert"),
                                  })
                                : t("label.activate-entity", {
                                      entity: t("label.alert"),
                                  })}
                        </MenuItem>

                        {/* Edit alert */}
                        <MenuItem onClick={handleAlertEdit}>
                            {t("label.edit-entity", {
                                entity: t("label.alert"),
                            })}
                        </MenuItem>

                        {/* Delete alert */}
                        <MenuItem onClick={OptionsDelete}>
                            {t("label.delete-entity", {
                                entity: t("label.alert"),
                            })}
                        </MenuItem>
                    </Menu>
                </Grid>
            </PageHeaderV1>

            <PageContentsGridV1>
                <Grid container>
                    {/* Alert */}

                    <Grid item xs={12}>
                        <AlertCardV1 uiAlert={uiAlert} />
                    </Grid>

                    {/* Alert evaluation time series */}
                    <Grid item xs={12}>
                        <AlertEvaluationTimeSeriesCard
                            alertEvaluation={alertEvaluation}
                            alertEvaluationTimeSeriesHeight={300}
                            maximizedTitle={uiAlert ? uiAlert.name : ""}
                            onRefresh={fetchAlertEvaluation}
                        />
                    </Grid>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
