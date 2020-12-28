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
} from "../../utils/routes-util/routes-util";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
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

    const onViewAnomalyDetails = (): void => {
        history.push(getAnomaliesDetailPath(props.anomaly.id));
    };

    const onInvestigateAnomaly = (): void => {
        // TODO
    };

    const onDeleteAnomaly = (): void => {
        props.onDelete && props.onDelete(props.anomaly);

        closeAnomalyOptions();
    };

    const onViewAlertDetails = (): void => {
        history.push(getAlertsDetailPath(props.anomaly.alertId));
    };

    const closeAnomalyOptions = (): void => {
        setAnomalyOptionsAnchorElement(null);
    };

    return (
        <Card variant="outlined">
            {/* Anomaly name */}
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
                                    text={props.anomaly.name}
                                />
                            </Link>
                        )}
                    </>
                }
            />

            <Menu
                anchorEl={anomalyOptionsAnchorElement}
                open={Boolean(anomalyOptionsAnchorElement)}
                onClose={closeAnomalyOptions}
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

            <CardContent>
                <Grid container>
                    <Grid container item md={12}>
                        {/* Alert */}
                        <Grid item md={4}>
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
                                    text={props.anomaly.alertName}
                                />
                            </Link>
                        </Grid>

                        {/* Current / Predicted */}
                        <Grid item md={4}>
                            <Typography variant="subtitle2">
                                {t("label.current-/-predicted")}
                            </Typography>

                            <Typography variant="body2">
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    text={t(
                                        "label.current-/-predicted-values",
                                        {
                                            current: props.anomaly.current,
                                            predicted: props.anomaly.predicted,
                                        }
                                    )}
                                />
                            </Typography>
                        </Grid>

                        {/* Deviation */}
                        <Grid item md={4}>
                            <Typography variant="subtitle2">
                                {t("label.deviation")}
                            </Typography>

                            <Typography
                                className={
                                    props.anomaly.negativeDeviation
                                        ? anomalyCardClasses.deviation
                                        : ""
                                }
                                variant="body2"
                            >
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    text={props.anomaly.deviation}
                                />
                            </Typography>
                        </Grid>
                    </Grid>

                    {/* Separator */}
                    <Grid item md={12}>
                        <Divider variant="fullWidth" />
                    </Grid>

                    <Grid container item md={12}>
                        {/* Duration */}
                        <Grid item md={4}>
                            <Typography variant="subtitle2">
                                {t("label.duration")}
                            </Typography>

                            <Typography variant="body2">
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    text={props.anomaly.duration}
                                />
                            </Typography>
                        </Grid>

                        {/* Start */}
                        <Grid item md={4}>
                            <Typography variant="subtitle2">
                                {t("label.start")}
                            </Typography>

                            <Typography variant="body2">
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    text={props.anomaly.startTime}
                                />
                            </Typography>
                        </Grid>

                        {/* End */}
                        <Grid item md={4}>
                            <Typography variant="subtitle2">
                                {t("label.end")}
                            </Typography>

                            <Typography variant="body2">
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    text={props.anomaly.endTime}
                                />
                            </Typography>
                        </Grid>
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
