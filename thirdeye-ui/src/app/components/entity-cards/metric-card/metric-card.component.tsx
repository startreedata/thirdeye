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
import { isEmpty } from "lodash";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { getMetricsDetailPath } from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { MetricCardProps } from "./metric-card.interfaces";
import { useMetricCardStyles } from "./metric-card.styles";

export const MetricCard: FunctionComponent<MetricCardProps> = (
    props: MetricCardProps
) => {
    const metricCardClasses = useMetricCardStyles();
    const [
        metricOptionsAnchorElement,
        setMetricOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const handleMetricOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setMetricOptionsAnchorElement(event.currentTarget);
    };

    const handleMetricOptionsClose = (): void => {
        setMetricOptionsAnchorElement(null);
    };

    const handleMetricViewDetails = (): void => {
        if (!props.metricCardData) {
            return;
        }

        history.push(getMetricsDetailPath(props.metricCardData.id));
        handleMetricOptionsClose();
    };

    const handleMetricDelete = (): void => {
        props.onDelete && props.onDelete(props.metricCardData);
        handleMetricOptionsClose();
    };

    return (
        <Card variant="outlined">
            {props.metricCardData && (
                <CardHeader
                    action={
                        <Grid container alignItems="center" spacing={0}>
                            {/* Active/inactive */}
                            <Grid item>
                                <Typography
                                    className={
                                        props.metricCardData.active
                                            ? metricCardClasses.active
                                            : metricCardClasses.inactive
                                    }
                                    variant="h6"
                                >
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.metricCardData.activeText}
                                    />
                                </Typography>
                            </Grid>

                            <Grid item>
                                {/* Metric options button */}
                                <IconButton onClick={handleMetricOptionsClick}>
                                    <MoreVertIcon />
                                </IconButton>

                                {/* Metric options */}
                                <Menu
                                    anchorEl={metricOptionsAnchorElement}
                                    open={Boolean(metricOptionsAnchorElement)}
                                    onClose={handleMetricOptionsClose}
                                >
                                    {/* View details */}
                                    {props.showViewDetails && (
                                        <MenuItem
                                            onClick={handleMetricViewDetails}
                                        >
                                            {t("label.view-details")}
                                        </MenuItem>
                                    )}

                                    {/* Delete metric */}
                                    <MenuItem onClick={handleMetricDelete}>
                                        {t("label.delete-entity", {
                                            entity: t("label.metric"),
                                        })}
                                    </MenuItem>
                                </Menu>
                            </Grid>
                        </Grid>
                    }
                    title={
                        <>
                            {/* Metric name */}
                            {props.showViewDetails && (
                                <Link onClick={handleMetricViewDetails}>
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.metricCardData.name}
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
                {props.metricCardData && (
                    <Grid container>
                        {/* Dataset */}
                        <Grid item md={4} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.dataset")}
                                searchWords={props.searchWords}
                                values={[props.metricCardData.datasetName]}
                            />
                        </Grid>

                        {/* Aggregation column */}
                        <Grid item md={4} xs={6}>
                            <NameValueDisplayCard<string>
                                name={t("label.aggregation-column")}
                                searchWords={props.searchWords}
                                values={[
                                    props.metricCardData.aggregationColumn,
                                ]}
                            />
                        </Grid>

                        {/* Aggregation function */}
                        <Grid item md={4} xs={6}>
                            <NameValueDisplayCard<string>
                                name={t("label.aggregation-function")}
                                searchWords={props.searchWords}
                                values={[
                                    props.metricCardData.aggregationFunction,
                                ]}
                            />
                        </Grid>

                        {!isEmpty(props.metricCardData.views) && (
                            <>
                                <Grid item xs={12}>
                                    <Grid container alignItems="center">
                                        {/* Views label */}
                                        <Grid item sm={1} xs={2}>
                                            <Typography variant="subtitle1">
                                                {t("label.views")}
                                            </Typography>
                                        </Grid>

                                        {/* Separator */}
                                        <Grid item sm={11} xs={10}>
                                            <Divider variant="fullWidth" />
                                        </Grid>
                                    </Grid>
                                </Grid>

                                {/* views */}
                                {props.metricCardData.views.map(
                                    (view, index) => (
                                        <Grid item key={index} xs={12}>
                                            <NameValueDisplayCard<string>
                                                wrap
                                                name={view.name}
                                                searchWords={props.searchWords}
                                                valueClassName={
                                                    metricCardClasses.query
                                                }
                                                values={[view.query]}
                                            />
                                        </Grid>
                                    )
                                )}
                            </>
                        )}
                    </Grid>
                )}

                {/* No data available */}
                {!props.metricCardData && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
