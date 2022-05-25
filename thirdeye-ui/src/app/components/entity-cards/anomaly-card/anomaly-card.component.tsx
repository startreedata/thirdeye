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
import MoreVertIcon from "@material-ui/icons/MoreVert";
import classnames from "classnames";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
    getAnomaliesAnomalyPath,
    getRootCauseAnalysisForAnomalyInvestigatePath,
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
    const [searchParams] = useSearchParams();
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

    const handleAnomalyDelete = (): void => {
        if (!props.uiAnomaly) {
            return;
        }

        props.onDelete && props.onDelete(props.uiAnomaly);
        handleAnomalyOptionsClose();
    };

    return (
        <Card variant="outlined">
            {props.uiAnomaly && (
                <CardHeader
                    action={
                        <>
                            <Button
                                color="primary"
                                component="button"
                                href={`${getRootCauseAnalysisForAnomalyInvestigatePath(
                                    props.uiAnomaly.id
                                )}?${searchParams.toString()}`}
                                variant="outlined"
                            >
                                {t("label.investigate-entity", {
                                    entity: t("label.anomaly"),
                                })}
                            </Button>
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
                        {/* Start */}
                        <Grid item>
                            <NameValueDisplayCard<string>
                                name={t("label.start")}
                                searchWords={props.searchWords}
                                values={[props.uiAnomaly.startTime]}
                            />
                        </Grid>

                        {/* End */}
                        <Grid item>
                            <NameValueDisplayCard<string>
                                name={t("label.end")}
                                searchWords={props.searchWords}
                                values={[props.uiAnomaly.endTime]}
                            />
                        </Grid>

                        {/* Duration */}
                        <Grid item>
                            <NameValueDisplayCard<string>
                                name={t("label.duration")}
                                searchWords={props.searchWords}
                                values={[props.uiAnomaly.duration]}
                            />
                        </Grid>

                        {/* Current/Predicted */}
                        <Grid item>
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
                        <Grid item>
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
