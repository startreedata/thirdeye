import {
    Card,
    CardContent,
    CardHeader,
    Divider,
    Grid,
    IconButton,
    Link,
    Menu,
    MenuItem,
} from "@material-ui/core";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import classnames from "classnames";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    getAlertsViewPath,
    getAnomaliesAnomalyPath,
} from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { AnomalyCardProps } from "./anomaly-card.interfaces";
import { useAnomalyCardStyles } from "./anomaly-card.styles";

export const AnomalyCard: FunctionComponent<AnomalyCardProps> = (
    props: AnomalyCardProps
) => {
    const anomalyCardClasses = useAnomalyCardStyles();
    const [anomalyOptionsAnchorElement, setAnomalyOptionsAnchorElement] =
        useState<HTMLElement | null>();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleAnomalyOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setAnomalyOptionsAnchorElement(event.currentTarget);
    };

    const handleAnomalyOptionsClose = (): void => {
        setAnomalyOptionsAnchorElement(null);
    };

    const handleAnomalyViewDetails = (): void => {
        if (!props.uiAnomaly) {
            return;
        }

        navigate(getAnomaliesAnomalyPath(props.uiAnomaly.id));
        handleAnomalyOptionsClose();
    };

    const handleAnomalyInvestigate = (): void => {
        handleAnomalyOptionsClose();
    };

    const handleAnomalyDelete = (): void => {
        if (!props.uiAnomaly) {
            return;
        }

        props.onDelete && props.onDelete(props.uiAnomaly);
        handleAnomalyOptionsClose();
    };

    const handleAlertViewDetails = (): void => {
        if (!props.uiAnomaly) {
            return;
        }

        navigate(getAlertsViewPath(props.uiAnomaly.alertId));
    };

    return (
        <Card variant="outlined">
            {props.uiAnomaly && (
                <CardHeader
                    action={
                        <>
                            {/* Anomaly options button */}
                            <IconButton
                                color="secondary"
                                onClick={handleAnomalyOptionsClick}
                            >
                                <MoreVertIcon />
                            </IconButton>

                            {/* Anomaly options */}
                            <Menu
                                anchorEl={anomalyOptionsAnchorElement}
                                open={Boolean(anomalyOptionsAnchorElement)}
                                onClose={handleAnomalyOptionsClose}
                            >
                                {/* View details */}
                                {props.showViewDetails && (
                                    <MenuItem
                                        onClick={handleAnomalyViewDetails}
                                    >
                                        {t("label.view-details")}
                                    </MenuItem>
                                )}

                                {/* Investigate anomaly */}
                                <MenuItem
                                    disabled
                                    onClick={handleAnomalyInvestigate}
                                >
                                    {t("label.investigate-entity", {
                                        entity: t("label.anomaly"),
                                    })}
                                </MenuItem>

                                {/* Delete anomaly */}
                                <MenuItem onClick={handleAnomalyDelete}>
                                    {t("label.delete-entity", {
                                        entity: t("label.anomaly"),
                                    })}
                                </MenuItem>
                            </Menu>
                        </>
                    }
                    title={
                        <>
                            {/* Anomaly name */}
                            {props.showViewDetails && (
                                <Link onClick={handleAnomalyViewDetails}>
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.uiAnomaly.name}
                                    />
                                </Link>
                            )}

                            {/* Summary */}
                            {!props.showViewDetails && t("label.summary")}
                        </>
                    }
                    titleTypographyProps={{ variant: "h6" }}
                />
            )}

            <CardContent>
                {props.uiAnomaly && (
                    <Grid container>
                        {/* Alert */}
                        <Grid item md={3} xs={6}>
                            <NameValueDisplayCard<string>
                                link
                                name={t("label.alert")}
                                searchWords={props.searchWords}
                                values={[props.uiAnomaly.alertName]}
                                onClick={handleAlertViewDetails}
                            />
                        </Grid>

                        {/* Duration */}
                        <Grid item md={3} xs={6}>
                            <NameValueDisplayCard<string>
                                name={t("label.duration")}
                                searchWords={props.searchWords}
                                values={[props.uiAnomaly.duration]}
                            />
                        </Grid>

                        {/* Separator */}
                        <Grid item xs={12}>
                            <Divider variant="fullWidth" />
                        </Grid>

                        {/* Start */}
                        <Grid item md={3} xs={6}>
                            <NameValueDisplayCard<string>
                                name={t("label.start")}
                                searchWords={props.searchWords}
                                values={[props.uiAnomaly.startTime]}
                            />
                        </Grid>

                        {/* End */}
                        <Grid item md={3} xs={6}>
                            <NameValueDisplayCard<string>
                                name={t("label.end")}
                                searchWords={props.searchWords}
                                values={[props.uiAnomaly.endTime]}
                            />
                        </Grid>

                        {/* Current/Predicted */}
                        <Grid item md={3} xs={6}>
                            <NameValueDisplayCard<string>
                                name={`${t("label.current")}${t(
                                    "label.pair-separator"
                                )}${t("label.predicted")}`}
                                searchWords={props.searchWords}
                                values={[
                                    `${props.uiAnomaly.current}${t(
                                        "label.pair-separator"
                                    )}${props.uiAnomaly.predicted}`,
                                ]}
                            />
                        </Grid>

                        {/* Deviation */}
                        <Grid item md={3} xs={6}>
                            <NameValueDisplayCard<string>
                                name={t("label.deviation")}
                                searchWords={props.searchWords}
                                valueClassName={classnames({
                                    [anomalyCardClasses.deviation]:
                                        props.uiAnomaly.negativeDeviation,
                                })}
                                values={[props.uiAnomaly.deviation]}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available */}
                {!props.uiAnomaly && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
