import {
    Button,
    Card,
    CardContent,
    CardHeader,
    Grid,
    IconButton,
    Link,
    Menu,
    MenuItem,
} from "@material-ui/core";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import {
    getAlertsUpdatePath,
    getAlertsViewPath,
} from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { AlertCardProps } from "./alert-card.interfaces";

export const AlertCard: FunctionComponent<AlertCardProps> = (
    props: AlertCardProps
) => {
    const [
        alertOptionsAnchorElement,
        setAlertOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const handleAlertOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setAlertOptionsAnchorElement(event.currentTarget);
    };

    const handleAlertOptionsClose = (): void => {
        setAlertOptionsAnchorElement(null);
    };

    const handleAlertViewDetails = (): void => {
        if (!props.uiAlert) {
            return;
        }

        history.push(getAlertsViewPath(props.uiAlert.id));
        handleAlertOptionsClose();
    };

    const handleAlertStateToggle = (): void => {
        if (!props.uiAlert || !props.uiAlert.alert) {
            return;
        }

        props.uiAlert.alert.active = !props.uiAlert.alert.active;
        props.onChange && props.onChange(props.uiAlert);
        handleAlertOptionsClose();
    };

    const handleAlertEdit = (): void => {
        if (!props.uiAlert) {
            return;
        }

        history.push(getAlertsUpdatePath(props.uiAlert.id));
        handleAlertOptionsClose();
    };

    const handleAlertDelete = (): void => {
        if (!props.uiAlert) {
            return;
        }

        props.onDelete && props.onDelete(props.uiAlert);
        handleAlertOptionsClose();
    };

    const anomalies: Anomaly[] =
        props.alertEvaluation.detectionEvaluations
            .output_AnomalyDetectorResult_0.anomalies;

    return (
        <Card variant="outlined">
            {props.uiAlert && (
                <CardHeader
                    action={
                        <Grid container alignItems="center" spacing={2}>
                            {/* Active/inactive */}
                            <Grid item>
                                <Button
                                    disableRipple
                                    startIcon={
                                        props.uiAlert.active ? (
                                            <CheckIcon color="primary" />
                                        ) : (
                                            <CloseIcon color="error" />
                                        )
                                    }
                                >
                                    {t(
                                        `label.${
                                            props.uiAlert.active
                                                ? "active"
                                                : "inactive"
                                        }`
                                    )}
                                </Button>
                            </Grid>

                            <Grid item>
                                {/* Alert options button */}
                                <IconButton onClick={handleAlertOptionsClick}>
                                    <MoreVertIcon />
                                </IconButton>

                                {/* Alert options */}
                                <Menu
                                    anchorEl={alertOptionsAnchorElement}
                                    open={Boolean(alertOptionsAnchorElement)}
                                    onClose={handleAlertOptionsClose}
                                >
                                    {/* View details */}
                                    {props.showViewDetails && (
                                        <MenuItem
                                            onClick={handleAlertViewDetails}
                                        >
                                            {t("label.view-details")}
                                        </MenuItem>
                                    )}

                                    {/* Activate/deactivate alert */}
                                    <MenuItem onClick={handleAlertStateToggle}>
                                        {props.uiAlert.active
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
                                    <MenuItem onClick={handleAlertDelete}>
                                        {t("label.delete-entity", {
                                            entity: t("label.alert"),
                                        })}
                                    </MenuItem>
                                </Menu>
                            </Grid>
                        </Grid>
                    }
                    title={
                        <>
                            {/* Alert name */}
                            {props.showViewDetails && (
                                <Link onClick={handleAlertViewDetails}>
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.uiAlert.name}
                                    />
                                </Link>
                            )}

                            {/* Summary */}
                            {!props.showViewDetails &&
                                t("label.alert-performance")}
                        </>
                    }
                    titleTypographyProps={{ variant: "h6" }}
                />
            )}

            <CardContent>
                {props.uiAlert && (
                    <Grid container>
                        {/* Created by */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.anomalies")}
                                searchWords={props.searchWords}
                                values={[`${anomalies ? anomalies.length : 0}`]}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available */}
                {!props.uiAlert && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
