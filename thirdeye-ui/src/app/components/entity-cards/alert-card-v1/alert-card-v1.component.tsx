import { Grid, Typography } from "@material-ui/core";
import { forEach } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { UiAlert } from "../../../rest/dto/ui-alert.interfaces";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { useAlertCardStyles } from "./alert-card-v1-styles";
import { AlertCardV1Props } from "./alert-card-v1.interfaces";

export const AlertCardV1: FunctionComponent<AlertCardV1Props> = (
    props: AlertCardV1Props
) => {
    const classes = useAlertCardStyles();
    const { t } = useTranslation();

    const getAllSubscriptionGroupsName = (uiAlert: UiAlert): string => {
        const subScriptingGroups: Array<string | number> = [];
        forEach(uiAlert.subscriptionGroups, (Obj) => {
            if (Obj.name) {
                subScriptingGroups.push(Obj.name);
            }
        });

        return subScriptingGroups.join(", ");
    };

    const getAllDatasetAndMetrics = (uiAlert: UiAlert): string => {
        const datasetMetrics: Array<string | number> = [];
        forEach(uiAlert.datasetAndMetrics, (Obj) => {
            if (Obj.datasetName && Obj.metricName) {
                datasetMetrics.push(`${Obj.datasetName}/${Obj.metricName}`);
            }
        });

        return datasetMetrics.join(", ");
    };

    const getDetectionTypes = (uiAlert: UiAlert): string => {
        return uiAlert.detectionTypes.join(", ");
    };

    if (!props.uiAlert) {
        return <NoDataIndicator />;
    }

    return (
        <Grid container>
            {/* First Column */}
            <Grid container item alignContent="flex-start" md={6} xs={12}>
                {/* Created By */}
                {props.showCreatedBy && (
                    <>
                        <Grid item xs={4}>
                            <Typography
                                className={classes.fontMedium}
                                variant="body2"
                            >
                                {t("label.created-by")}:
                            </Typography>
                        </Grid>
                        <Grid item xs={8}>
                            <Typography variant="body2">
                                {props.uiAlert.createdBy || "-"}
                            </Typography>
                        </Grid>
                    </>
                )}

                {/* Detection Type */}
                <Grid item xs={4}>
                    <Typography className={classes.fontMedium} variant="body2">
                        {t("label.detection-type")}:
                    </Typography>
                </Grid>

                <Grid item xs={8}>
                    <Typography variant="body2">
                        {getDetectionTypes(props.uiAlert) || "-"}
                    </Typography>
                </Grid>

                {/* Dataset/Metric */}
                <Grid item xs={4}>
                    <Typography className={classes.fontMedium} variant="body2">
                        {t("label.dataset-metric")}:
                    </Typography>
                </Grid>
                <Grid item xs={8}>
                    <Typography variant="body2">
                        {getAllDatasetAndMetrics(props.uiAlert) || "-"}
                    </Typography>
                </Grid>
            </Grid>

            {/* second column */}
            <Grid container item alignContent="flex-start" md={6} xs={12}>
                {/* Subscription Groups */}
                <Grid item xs={4}>
                    <Typography className={classes.fontMedium} variant="body2">
                        {t("label.subscription-groups")}:
                    </Typography>
                </Grid>

                <Grid item xs={8}>
                    <Typography variant="body2">
                        {getAllSubscriptionGroupsName(props.uiAlert) || "-"}
                    </Typography>
                </Grid>
            </Grid>
        </Grid>
    );
};
