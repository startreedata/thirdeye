/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Button,
    Card,
    CardContent,
    CardHeader,
    Grid,
    IconButton,
    Link as MaterialLink,
    Menu,
    MenuItem,
} from "@material-ui/core";
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useNavigate } from "react-router-dom";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import {
    getAlertsUpdatePath,
    getAlertsViewPath,
    getAnomaliesAllRangePath,
} from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { AlertCardProps } from "./alert-card.interfaces";

export const AlertCard: FunctionComponent<AlertCardProps> = (
    props: AlertCardProps
) => {
    const [alertOptionsAnchorElement, setAlertOptionsAnchorElement] =
        useState<HTMLElement | null>();
    const navigate = useNavigate();
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

        navigate(getAlertsViewPath(props.uiAlert.id));
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

        navigate(getAlertsUpdatePath(props.uiAlert.id));
        handleAlertOptionsClose();
    };

    const handleAlertDelete = (): void => {
        if (!props.uiAlert) {
            return;
        }

        props.onDelete && props.onDelete(props.uiAlert);
        handleAlertOptionsClose();
    };

    const anomalies: Anomaly[] = props.anomalies || [];

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
                                <IconButton
                                    color="secondary"
                                    onClick={handleAlertOptionsClick}
                                >
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
                                <MaterialLink onClick={handleAlertViewDetails}>
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.uiAlert.name}
                                    />
                                </MaterialLink>
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
                        {/* Number of anomalies */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<string>
                                name={`${t("label.anomalies")} in ${t(
                                    "label.time-range"
                                )}`}
                                searchWords={props.searchWords}
                                valueRenderer={(value) => {
                                    if (value) {
                                        const filteredAnomaliesUrl =
                                            getAnomaliesAllRangePath(
                                                props.uiAlert?.name
                                            );

                                        return (
                                            <Link to={filteredAnomaliesUrl}>
                                                {value}
                                            </Link>
                                        );
                                    }

                                    return value;
                                }}
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
