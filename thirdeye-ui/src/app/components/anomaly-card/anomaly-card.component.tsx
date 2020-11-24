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
import SettingsIcon from "@material-ui/icons/Settings";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import Highlighter from "react-highlight-words";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { getAnomalyName } from "../../utils/anomaly/anomaly-util";
import {
    formatDuration,
    formatLongDateAndTime,
} from "../../utils/date-time/date-time-util";
import {
    getAlertsDetailPath,
    getAnomaliesDetailPath,
} from "../../utils/route/routes-util";
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
        history.push(getAlertsDetailPath(props.anomaly.alert.id));
    };

    const onAnomalyOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        if (!event) {
            return;
        }

        setOptionsAnchorElement(event.currentTarget);
    };

    const closeAnomalyOptions = (): void => {
        setOptionsAnchorElement(null);
    };

    return (
        <Card className={anomalyCardClasses.container} variant="outlined">
            {/* Anomaly name */}
            <CardHeader
                disableTypography
                action={
                    <IconButton onClick={onAnomalyOptionsClick}>
                        <SettingsIcon color="primary" />
                    </IconButton>
                }
                className={anomalyCardClasses.header}
                title={
                    <Link
                        component="button"
                        variant="h6"
                        onClick={onAnomalyDetails}
                    >
                        <Highlighter
                            highlightClassName={anomalyCardClasses.highlight}
                            searchWords={props.searchWords as string[]}
                            textToHighlight={getAnomalyName(props.anomaly)}
                        />
                    </Link>
                }
            />

            <Menu
                anchorEl={optionsAnchorElement}
                open={Boolean(optionsAnchorElement)}
                onClose={closeAnomalyOptions}
            >
                <MenuItem onClick={onAnomalyDetails}>
                    {t("label.view-details")}
                </MenuItem>
                <MenuItem onClick={onInvestigate}>
                    {t("label.investigate")}
                </MenuItem>
            </Menu>

            <CardContent>
                <Grid container>
                    <Grid container item>
                        <Grid item md={4}>
                            {/* Duration */}
                            <Typography variant="body2">
                                <strong>{t("label.duration")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        anomalyCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={formatDuration(
                                        props.anomaly.startTime,
                                        props.anomaly.endTime
                                    )}
                                />
                            </Typography>
                        </Grid>

                        {/* Start */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.start")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        anomalyCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={formatLongDateAndTime(
                                        props.anomaly.startTime
                                    )}
                                />
                            </Typography>
                        </Grid>

                        {/* End */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.end")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        anomalyCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={formatLongDateAndTime(
                                        props.anomaly.endTime
                                    )}
                                />
                            </Typography>
                        </Grid>
                    </Grid>

                    <Divider className={anomalyCardClasses.divider} />

                    {/* Alert */}
                    <Grid container item>
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.alert")}</strong>
                            </Typography>
                            <Link
                                component="button"
                                variant="body2"
                                onClick={onAlertDetails}
                            >
                                <Highlighter
                                    highlightClassName={
                                        anomalyCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={props.anomaly.alert.name}
                                />
                            </Link>
                        </Grid>

                        {/* Current */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.current")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        anomalyCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={props.anomaly.avgCurrentVal.toString()}
                                />
                            </Typography>
                        </Grid>

                        {/* Predicted */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.predicted")}</strong>
                            </Typography>
                            <Typography variant="body2">
                                <Highlighter
                                    highlightClassName={
                                        anomalyCardClasses.highlight
                                    }
                                    searchWords={props.searchWords as string[]}
                                    textToHighlight={props.anomaly.avgBaselineVal.toString()}
                                />
                            </Typography>
                        </Grid>
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
