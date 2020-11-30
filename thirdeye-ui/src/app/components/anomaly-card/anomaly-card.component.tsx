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
import MoreVertIcon from "@material-ui/icons/MoreVert";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getAnomaliesDetailPath,
} from "../../utils/route/routes-util";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import { AnomalyCardProps } from "./anomaly-card.interfaces";
import { useAnomalyCardStyles } from "./anomaly-card.styles";

export const AnomalyCard: FunctionComponent<AnomalyCardProps> = (
    props: AnomalyCardProps
) => {
    const anomalyCardClasses = useAnomalyCardStyles();
    const [
        optionsAnchorElement,
        setOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const onAnomalyDetails = (): void => {
        history.push(getAnomaliesDetailPath(props.anomaly.id));
    };

    const onInvestigate = (): void => {
        // TODO
    };

    const onAlertDetails = (): void => {
        history.push(getAlertsDetailPath(props.anomaly.alertId));
    };

    const onAnomalyOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setOptionsAnchorElement(event.currentTarget);
    };

    const closeAnomalyOptions = (): void => {
        setOptionsAnchorElement(null);
    };

    return (
        <Card variant="outlined">
            {/* Anomaly name */}
            <CardHeader
                disableTypography
                action={
                    <IconButton onClick={onAnomalyOptionsClick}>
                        <MoreVertIcon />
                    </IconButton>
                }
                title={
                    (props.hideViewDetailsLinks && (
                        <Typography variant="h6">
                            {t("label.summary")}
                        </Typography>
                    )) || (
                        <Link
                            component="button"
                            variant="h6"
                            onClick={onAnomalyDetails}
                        >
                            <TextHighlighter
                                searchWords={props.searchWords}
                                textToHighlight={props.anomaly.name}
                            />
                        </Link>
                    )
                }
            />

            <Menu
                anchorEl={optionsAnchorElement}
                open={Boolean(optionsAnchorElement)}
                onClose={closeAnomalyOptions}
            >
                {!props.hideViewDetailsLinks && (
                    <MenuItem onClick={onAnomalyDetails}>
                        {t("label.view-details")}
                    </MenuItem>
                )}
                <MenuItem onClick={onInvestigate}>
                    {t("label.investigate")}
                </MenuItem>
            </Menu>

            <CardContent>
                <Grid container>
                    <Grid container item>
                        {/* Alert */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.alert")}</strong>
                            </Typography>
                            <Link
                                component="button"
                                variant="body2"
                                onClick={onAlertDetails}
                            >
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    textToHighlight={props.anomaly.alertName}
                                />
                            </Link>
                        </Grid>

                        {/* Current / Predicted */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>
                                    {t("label.current-/-predicted")}
                                </strong>
                            </Typography>
                            <Typography variant="body2">
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    textToHighlight={t(
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
                            <Typography variant="body2">
                                <strong>{t("label.deviation")}</strong>
                            </Typography>
                            <Typography
                                className={
                                    props.anomaly.negativeDeviation
                                        ? anomalyCardClasses.deviationError
                                        : ""
                                }
                                variant="body2"
                            >
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    textToHighlight={props.anomaly.deviation}
                                />
                            </Typography>
                        </Grid>
                    </Grid>

                    <Grid item md={12}>
                        <Divider variant="fullWidth" />
                    </Grid>

                    <Grid container item>
                        {/* Duration */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.duration")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    textToHighlight={props.anomaly.duration}
                                />
                            </Typography>
                        </Grid>

                        {/* Start */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.start")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    textToHighlight={props.anomaly.startTime}
                                />
                            </Typography>
                        </Grid>

                        {/* End */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.end")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    textToHighlight={props.anomaly.endTime}
                                />
                            </Typography>
                        </Grid>
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
