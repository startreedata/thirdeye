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
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getAnomaliesDetailPath,
} from "../../../utils/routes/routes.util";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
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

    const handleAnomalyOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setAnomalyOptionsAnchorElement(event.currentTarget);
    };

    const handleAnomalyOptionsClose = (): void => {
        setAnomalyOptionsAnchorElement(null);
    };

    const handleAnomalyViewDetails = (): void => {
        history.push(getAnomaliesDetailPath(props.anomalyCardData.id));
        handleAnomalyOptionsClose();
    };

    const handleAnomalyInvestigate = (): void => {
        handleAnomalyOptionsClose();
    };

    const handleAnomalyDelete = (): void => {
        props.onDelete && props.onDelete(props.anomalyCardData);
        handleAnomalyOptionsClose();
    };

    const handleAlertViewDetails = (): void => {
        history.push(getAlertsDetailPath(props.anomalyCardData.alertId));
    };

    return (
        <Card variant="outlined">
            <CardHeader
                action={
                    <>
                        {/* Anomaly options button */}
                        <IconButton onClick={handleAnomalyOptionsClick}>
                            <MoreVertIcon />
                        </IconButton>

                        <Menu
                            anchorEl={anomalyOptionsAnchorElement}
                            open={Boolean(anomalyOptionsAnchorElement)}
                            onClose={handleAnomalyOptionsClose}
                        >
                            {/* View details */}
                            {props.showViewDetails && (
                                <MenuItem onClick={handleAnomalyViewDetails}>
                                    {t("label.view-details")}
                                </MenuItem>
                            )}

                            {/* Investigate anomaly*/}
                            <MenuItem
                                disabled
                                onClick={handleAnomalyInvestigate}
                            >
                                {t("label.investigate-anomaly")}
                            </MenuItem>

                            {/* Delete anomaly */}
                            <MenuItem onClick={handleAnomalyDelete}>
                                {t("label.delete-anomaly")}
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
                                    text={props.anomalyCardData.name}
                                />
                            </Link>
                        )}

                        {/* Summary */}
                        {!props.showViewDetails && t("label.summary")}
                    </>
                }
                titleTypographyProps={{ variant: "h6" }}
            />

            <CardContent>
                <Grid container>
                    {/* Alert */}
                    <Grid item md={3} xs={6}>
                        <NameValueDisplayCard<string>
                            link
                            name={t("label.alert")}
                            searchWords={props.searchWords}
                            values={[props.anomalyCardData.alertName]}
                            onClick={handleAlertViewDetails}
                        />
                    </Grid>

                    {/* Duration */}
                    <Grid item md={3} xs={6}>
                        <NameValueDisplayCard<string>
                            name={t("label.duration")}
                            searchWords={props.searchWords}
                            values={[props.anomalyCardData.duration]}
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
                            values={[props.anomalyCardData.startTime]}
                        />
                    </Grid>

                    {/* End */}
                    <Grid item md={3} xs={6}>
                        <NameValueDisplayCard<string>
                            name={t("label.end")}
                            searchWords={props.searchWords}
                            values={[props.anomalyCardData.endTime]}
                        />
                    </Grid>

                    {/* Current / Predicted */}
                    <Grid item md={3} xs={6}>
                        <NameValueDisplayCard<string>
                            name={t("label.current-/-predicted")}
                            searchWords={props.searchWords}
                            values={[
                                t("label.current-/-predicted-values", {
                                    current: props.anomalyCardData.current,
                                    predicted: props.anomalyCardData.predicted,
                                }),
                            ]}
                        />
                    </Grid>

                    {/* Deviation */}
                    <Grid item md={3} xs={6}>
                        <NameValueDisplayCard<string>
                            name={t("label.deviation")}
                            searchWords={props.searchWords}
                            valueClassName={
                                props.anomalyCardData.negativeDeviation
                                    ? anomalyCardClasses.deviation
                                    : ""
                            }
                            values={[props.anomalyCardData.deviation]}
                        />
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
