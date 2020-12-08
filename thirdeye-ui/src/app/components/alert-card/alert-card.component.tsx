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
import { ExpandLess, ExpandMore, MoreVert } from "@material-ui/icons";
import { isEmpty } from "lodash";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import {
    getAlertsDetailPath,
    getAlertsUpdatePath,
} from "../../utils/routes-util/routes-util";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import { AlertCardProps } from "./alert-card.interfaces";
import { useAlertCardStyles } from "./alert-card.styles";

export const AlertCard: FunctionComponent<AlertCardProps> = (
    props: AlertCardProps
) => {
    const alertCardClasses = useAlertCardStyles();
    const [expanded, setExpanded] = useState(false);
    const [
        optionsAnchorElement,
        setOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const history = useHistory();
    const { t } = useTranslation();

    const onAlertDetails = (): void => {
        history.push(getAlertsDetailPath(props.alert.id));
    };

    const onAlertEdit = (): void => {
        history.push(getAlertsUpdatePath(props.alert.id));
    };

    const onAlertStateToggle = (): void => {
        props.onAlertStateToggle && props.onAlertStateToggle(props.alert);

        closeAlertOptions();
    };

    const onAlertOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setOptionsAnchorElement(event.currentTarget);
    };

    const closeAlertOptions = (): void => {
        setOptionsAnchorElement(null);
    };

    const onExpandToggle = (): void => {
        setExpanded(!expanded);
    };

    return (
        <Card variant="outlined">
            {/* Alert name */}
            <CardHeader
                disableTypography
                action={
                    <Grid container alignItems="center">
                        <Grid item>
                            <Typography
                                className={
                                    props.alert.active
                                        ? alertCardClasses.activeText
                                        : alertCardClasses.inactiveText
                                }
                                variant="h6"
                            >
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    textToHighlight={props.alert.activeText}
                                />
                            </Typography>
                        </Grid>

                        <Grid item>
                            <IconButton onClick={onAlertOptionsClick}>
                                <MoreVert />
                            </IconButton>
                        </Grid>
                    </Grid>
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
                            onClick={onAlertDetails}
                        >
                            <TextHighlighter
                                searchWords={props.searchWords}
                                textToHighlight={props.alert.name}
                            />
                        </Link>
                    )
                }
            />

            <Menu
                anchorEl={optionsAnchorElement}
                open={Boolean(optionsAnchorElement)}
                onClose={closeAlertOptions}
            >
                {/* View details */}
                {!props.hideViewDetailsLinks && (
                    <MenuItem onClick={onAlertDetails}>
                        {t("label.view-details")}
                    </MenuItem>
                )}

                {/* Edit alert */}
                <MenuItem onClick={onAlertEdit}>
                    {t("label.edit-alert")}
                </MenuItem>

                {/* Activete/deactivete alert */}
                <MenuItem onClick={onAlertStateToggle}>
                    {props.alert.active
                        ? t("label.deactivate-alert")
                        : t("label.activate-alert")}
                </MenuItem>
            </Menu>

            <CardContent>
                <Grid container>
                    <Grid container item md={12}>
                        {/* Created by */}
                        <Grid item md={4}>
                            <Typography variant="body2">
                                <strong>{t("label.created-by")}</strong>
                            </Typography>

                            <TextHighlighter
                                searchWords={props.searchWords}
                                textToHighlight={props.alert.createdBy}
                            />
                        </Grid>
                    </Grid>

                    <Grid item md={12}>
                        <Divider variant="fullWidth" />
                    </Grid>

                    <Grid container item md={12}>
                        {/* Detection type */}
                        <Grid item md={3}>
                            <div className={alertCardClasses.bottomRowLabel}>
                                <Typography variant="body2">
                                    <strong>{t("label.detection-type")}</strong>
                                </Typography>
                            </div>

                            {!isEmpty(props.alert.detectionTypes) &&
                                props.alert.detectionTypes.length > 1 && (
                                    <div
                                        className={
                                            alertCardClasses.bottomRowIcon
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandToggle}
                                        >
                                            {(expanded && (
                                                <ExpandLess
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )) || (
                                                <ExpandMore
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )}
                                        </Link>
                                    </div>
                                )}

                            <div className={alertCardClasses.bottomRowValue}>
                                {(isEmpty(props.alert.detectionTypes) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            textToHighlight={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )) ||
                                    (expanded && (
                                        <>
                                            {props.alert.detectionTypes.map(
                                                (detectionType) => (
                                                    <Typography
                                                        key={detectionType}
                                                        variant="body2"
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            textToHighlight={
                                                                detectionType
                                                            }
                                                        />
                                                    </Typography>
                                                )
                                            )}
                                        </>
                                    )) || (
                                        <Typography variant="body2">
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                textToHighlight={
                                                    props.alert
                                                        .detectionTypes[0]
                                                }
                                            />
                                        </Typography>
                                    )}
                            </div>
                        </Grid>

                        {/* Dataset / Metric */}
                        <Grid item md={3}>
                            <div className={alertCardClasses.bottomRowLabel}>
                                <Typography variant="body2">
                                    <strong>
                                        {t("label.dataset-/-metric")}
                                    </strong>
                                </Typography>
                            </div>

                            {!isEmpty(props.alert.datasetAndMetrics) &&
                                props.alert.datasetAndMetrics.length > 1 && (
                                    <div
                                        className={
                                            alertCardClasses.bottomRowIcon
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandToggle}
                                        >
                                            {(expanded && (
                                                <ExpandLess
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )) || (
                                                <ExpandMore
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )}
                                        </Link>
                                    </div>
                                )}

                            <div className={alertCardClasses.bottomRowValue}>
                                {(isEmpty(props.alert.datasetAndMetrics) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            textToHighlight={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )) ||
                                    (expanded && (
                                        <>
                                            {props.alert.datasetAndMetrics.map(
                                                (datasetAndMetric) => (
                                                    <Typography
                                                        key={
                                                            datasetAndMetric.metricId
                                                        }
                                                        variant="body2"
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            textToHighlight={t(
                                                                "label.dataset-/-metric-values",
                                                                {
                                                                    dataset:
                                                                        datasetAndMetric.datasetName,
                                                                    metric:
                                                                        datasetAndMetric.metricName,
                                                                }
                                                            )}
                                                        />
                                                    </Typography>
                                                )
                                            )}
                                        </>
                                    )) || (
                                        <Typography variant="body2">
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                textToHighlight={t(
                                                    "label.dataset-/-metric-values",
                                                    {
                                                        dataset:
                                                            props.alert
                                                                .datasetAndMetrics[0]
                                                                .datasetName,
                                                        metric:
                                                            props.alert
                                                                .datasetAndMetrics[0]
                                                                .metricName,
                                                    }
                                                )}
                                            />
                                        </Typography>
                                    )}
                            </div>
                        </Grid>

                        {/* Filtered by */}
                        <Grid item md={3}>
                            <div className={alertCardClasses.bottomRowLabel}>
                                <Typography variant="body2">
                                    <strong>{t("label.filtered-by")}</strong>
                                </Typography>
                            </div>

                            {!isEmpty(props.alert.filteredBy) &&
                                props.alert.filteredBy.length > 1 && (
                                    <div
                                        className={
                                            alertCardClasses.bottomRowIcon
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandToggle}
                                        >
                                            {(expanded && (
                                                <ExpandLess
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )) || (
                                                <ExpandMore
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )}
                                        </Link>
                                    </div>
                                )}

                            <div className={alertCardClasses.bottomRowValue}>
                                {(isEmpty(props.alert.filteredBy) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            textToHighlight={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )) ||
                                    (expanded && (
                                        <>
                                            {props.alert.filteredBy.map(
                                                (filteredBy) => (
                                                    <Typography
                                                        key={filteredBy}
                                                        variant="body2"
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            textToHighlight={
                                                                filteredBy
                                                            }
                                                        />
                                                    </Typography>
                                                )
                                            )}
                                        </>
                                    )) || (
                                        <Typography variant="body2">
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                textToHighlight={
                                                    props.alert.filteredBy[0]
                                                }
                                            />
                                        </Typography>
                                    )}
                            </div>
                        </Grid>

                        {/* Subscription groups */}
                        <Grid item md={3}>
                            <div className={alertCardClasses.bottomRowLabel}>
                                <Typography variant="body2">
                                    <strong>
                                        {t("label.subscription-groups")}
                                    </strong>
                                </Typography>
                            </div>

                            {!isEmpty(props.alert.subscriptionGroups) &&
                                props.alert.subscriptionGroups.length > 1 && (
                                    <div
                                        className={
                                            alertCardClasses.bottomRowIcon
                                        }
                                    >
                                        <Link
                                            component="button"
                                            onClick={onExpandToggle}
                                        >
                                            {(expanded && (
                                                <ExpandLess
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )) || (
                                                <ExpandMore
                                                    color="primary"
                                                    fontSize="small"
                                                />
                                            )}
                                        </Link>
                                    </div>
                                )}

                            <div className={alertCardClasses.bottomRowValue}>
                                {(isEmpty(props.alert.subscriptionGroups) && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            textToHighlight={t(
                                                "label.no-data-available-marker"
                                            )}
                                        />
                                    </Typography>
                                )) ||
                                    (expanded && (
                                        <>
                                            {props.alert.subscriptionGroups.map(
                                                (subscriptionGroup) => (
                                                    <Typography
                                                        key={
                                                            subscriptionGroup.id
                                                        }
                                                        variant="body2"
                                                    >
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            textToHighlight={
                                                                subscriptionGroup.name
                                                            }
                                                        />
                                                    </Typography>
                                                )
                                            )}
                                        </>
                                    )) || (
                                        <Typography variant="body2">
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                textToHighlight={
                                                    props.alert
                                                        .subscriptionGroups[0]
                                                        .name
                                                }
                                            />
                                        </Typography>
                                    )}
                            </div>
                        </Grid>
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
