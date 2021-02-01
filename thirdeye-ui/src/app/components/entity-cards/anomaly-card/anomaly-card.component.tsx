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
    Typography,
} from "@material-ui/core";
import { MoreVert } from "@material-ui/icons";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getAnomaliesDetailPath,
} from "../../../utils/routes-util/routes-util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { AnomalyCardProps } from "./anomaly-card.interfaces";
import { useAnomalyCardStyles } from "./anomaly-card.styles";

export const AnomalyCard: FunctionComponent<AnomalyCardProps> = (
    props: AnomalyCardProps
) => {
    const anomalyCardClasses = useAnomalyCardStyles();
    const [
        anomalyOptionsAnchorElement,
        setAnomalyOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const onAnomalyOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setAnomalyOptionsAnchorElement(event.currentTarget);
    };

    const onCloseAnomalyOptions = (): void => {
        setAnomalyOptionsAnchorElement(null);
    };

    const onViewAnomalyDetails = (): void => {
        history.push(getAnomaliesDetailPath(props.anomalyCardData.id));

        onCloseAnomalyOptions();
    };

    const onInvestigateAnomaly = (): void => {
        // TODO

        onCloseAnomalyOptions();
    };

    const onDeleteAnomaly = (): void => {
        props.onDelete && props.onDelete(props.anomalyCardData);

        onCloseAnomalyOptions();
    };

    const onViewAlertDetails = (): void => {
        history.push(getAlertsDetailPath(props.anomalyCardData.alertId));
    };

    return (
        <Card variant="outlined">
            {props.anomalyCardData && (
                <>
                    <CardHeader
                        disableTypography
                        action={
                            // Anomaly options button
                            <IconButton onClick={onAnomalyOptionsClick}>
                                <MoreVert />
                            </IconButton>
                        }
                        title={
                            <>
                                {/* Summary */}
                                {props.hideViewDetailsLinks && (
                                    <Typography variant="h6">
                                        {t("label.summary")}
                                    </Typography>
                                )}

                                {/* Anomaly name */}
                                {!props.hideViewDetailsLinks && (
                                    <Link
                                        component="button"
                                        variant="h6"
                                        onClick={onViewAnomalyDetails}
                                    >
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={props.anomalyCardData.name}
                                        />
                                    </Link>
                                )}
                            </>
                        }
                    />

                    <Menu
                        anchorEl={anomalyOptionsAnchorElement}
                        open={Boolean(anomalyOptionsAnchorElement)}
                        onClose={onCloseAnomalyOptions}
                    >
                        {/* View details */}
                        {!props.hideViewDetailsLinks && (
                            <MenuItem onClick={onViewAnomalyDetails}>
                                {t("label.view-details")}
                            </MenuItem>
                        )}

                        {/* Investigate anomaly*/}
                        <MenuItem onClick={onInvestigateAnomaly}>
                            {t("label.investigate-anomaly")}
                        </MenuItem>

                        {/* Delete anomaly */}
                        <MenuItem onClick={onDeleteAnomaly}>
                            {t("label.delete-anomaly")}
                        </MenuItem>
                    </Menu>
                </>
            )}

            <CardContent>
                {props.anomalyCardData && (
                    <Grid container>
                        <Grid container item sm={12}>
                            {/* Alert */}
                            <Grid item sm={4}>
                                <Typography variant="subtitle2">
                                    {t("label.alert")}
                                </Typography>

                                <Link
                                    component="button"
                                    display="block"
                                    variant="body2"
                                    onClick={onViewAlertDetails}
                                >
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.anomalyCardData.alertName}
                                    />
                                </Link>
                            </Grid>

                            {/* Current / Predicted */}
                            <Grid item sm={4}>
                                <Typography variant="subtitle2">
                                    {t("label.current-/-predicted")}
                                </Typography>

                                <Typography variant="body2">
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={t(
                                            "label.current-/-predicted-values",
                                            {
                                                current:
                                                    props.anomalyCardData
                                                        .current,
                                                predicted:
                                                    props.anomalyCardData
                                                        .predicted,
                                            }
                                        )}
                                    />
                                </Typography>
                            </Grid>

                            {/* Deviation */}
                            <Grid item sm={4}>
                                <Typography variant="subtitle2">
                                    {t("label.deviation")}
                                </Typography>

                                <Typography
                                    className={
                                        props.anomalyCardData.negativeDeviation
                                            ? anomalyCardClasses.deviation
                                            : ""
                                    }
                                    variant="body2"
                                >
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.anomalyCardData.deviation}
                                    />
                                </Typography>
                            </Grid>
                        </Grid>

                        {/* Separator */}
                        <Grid item sm={12}>
                            <Divider variant="fullWidth" />
                        </Grid>

                        <Grid container item sm={12}>
                            {/* Duration */}
                            <Grid item sm={4}>
                                <Typography variant="subtitle2">
                                    {t("label.duration")}
                                </Typography>

                                <Typography variant="body2">
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.anomalyCardData.duration}
                                    />
                                </Typography>
                            </Grid>

                            {/* Start */}
                            <Grid item sm={4}>
                                <Typography variant="subtitle2">
                                    {t("label.start")}
                                </Typography>

                                <Typography variant="body2">
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.anomalyCardData.startTime}
                                    />
                                </Typography>
                            </Grid>

                            {/* End */}
                            <Grid item sm={4}>
                                <Typography variant="subtitle2">
                                    {t("label.end")}
                                </Typography>

                                <Typography variant="body2">
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.anomalyCardData.endTime}
                                    />
                                </Typography>
                            </Grid>
                        </Grid>
                    </Grid>
                )}

                {/* No data available message */}
                {!props.anomalyCardData && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
