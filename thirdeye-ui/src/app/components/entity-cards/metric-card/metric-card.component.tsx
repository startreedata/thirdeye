import {
    Button,
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
import CheckIcon from "@material-ui/icons/Check";
import CloseIcon from "@material-ui/icons/Close";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import { isEmpty } from "lodash";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { getMetricsViewPath } from "../../../utils/routes/routes.util";
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
        if (!props.metric) {
            return;
        }

        history.push(getMetricsViewPath(props.metric.id));
        handleMetricOptionsClose();
    };

    const handleMetricDelete = (): void => {
        if (!props.metric) {
            return;
        }

        props.onDelete && props.onDelete(props.metric);
        handleMetricOptionsClose();
    };

    const handleMetricEdit = (): void => {
        if (!props.metric) {
            return;
        }

        props.onEdit && props.onEdit(props.metric.id);
        handleMetricOptionsClose();
    };

    return (
        <Card variant="outlined">
            {props.metric && (
                <CardHeader
                    action={
                        <Grid container alignItems="center" spacing={2}>
                            {/* Active/inactive */}
                            <Grid item>
                                <Button
                                    disableRipple
                                    startIcon={
                                        props.metric.active ? (
                                            <CheckIcon color="primary" />
                                        ) : (
                                            <CloseIcon color="error" />
                                        )
                                    }
                                >
                                    {t(
                                        `label.${
                                            props.metric.active
                                                ? "active"
                                                : "inactive"
                                        }`
                                    )}
                                </Button>
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

                                    {/* Edit metric */}
                                    <MenuItem onClick={handleMetricEdit}>
                                        {t("label.edit-entity", {
                                            entity: t("label.metric"),
                                        })}
                                    </MenuItem>

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
                                        text={props.metric.name}
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
                {props.metric && (
                    <Grid container>
                        {/* Dataset */}
                        <Grid item md={4} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.dataset")}
                                searchWords={props.searchWords}
                                values={[props.metric.datasetName]}
                            />
                        </Grid>

                        {/* Aggregation column */}
                        <Grid item md={4} xs={6}>
                            <NameValueDisplayCard<string>
                                name={t("label.aggregation-column")}
                                searchWords={props.searchWords}
                                values={[props.metric.aggregationColumn]}
                            />
                        </Grid>

                        {/* Aggregation function */}
                        <Grid item md={4} xs={6}>
                            <NameValueDisplayCard<string>
                                name={t("label.aggregation-function")}
                                searchWords={props.searchWords}
                                values={[props.metric.aggregationFunction]}
                            />
                        </Grid>

                        {!isEmpty(props.metric.views) && (
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
                                {props.metric.views.map((view, index) => (
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
                                ))}
                            </>
                        )}
                    </Grid>
                )}

                {/* No data available */}
                {!props.metric && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
