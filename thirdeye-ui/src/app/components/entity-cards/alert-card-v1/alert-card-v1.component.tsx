import { Grid, Typography } from "@material-ui/core";
import { capitalize, forEach } from "lodash";
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

    const getDetectionTypes = (uiAlert: UiAlert): string => {
        return uiAlert.detectionTypes.join(", ");
    };

    if (!props.uiAlert) {
        return <NoDataIndicator />;
    }

    return (
        <Grid container>
            {/* Created By */}
            {props.showCreatedBy && (
                <Grid item xs={6}>
                    <Grid item>
                        <Typography
                            className={classes.fontMedium}
                            variant="body2"
                        >
                            {t("label.created-by")}:
                        </Typography>
                    </Grid>
                    <Grid item>
                        <Typography variant="body2">
                            {props.uiAlert.createdBy || "-"}
                        </Typography>
                    </Grid>
                </Grid>
            )}

            {/* Detection Type */}
            {props.uiAlert.detectionTypes.length > 0 && (
                <Grid item xs={6}>
                    <Grid item>
                        <Typography
                            className={classes.fontMedium}
                            variant="body2"
                        >
                            {t("label.detection-type")}:
                        </Typography>
                    </Grid>

                    <Grid item>
                        <Typography variant="body2">
                            {getDetectionTypes(props.uiAlert) || "-"}
                        </Typography>
                    </Grid>
                </Grid>
            )}

            {/* Subscription Groups */}
            <Grid item xs={6}>
                <Grid item>
                    <Typography className={classes.fontMedium} variant="body2">
                        {t("label.subscription-groups")}:
                    </Typography>
                </Grid>

                <Grid item>
                    <Typography variant="body2">
                        {props.uiAlert.subscriptionGroups.length > 0 &&
                            getAllSubscriptionGroupsName(props.uiAlert)}
                        {props.uiAlert.subscriptionGroups.length === 0 && (
                            <span>None</span>
                        )}
                    </Typography>
                </Grid>
            </Grid>

            {props.uiAlert.renderedMetadata.length > 0 &&
                props.uiAlert.renderedMetadata.map((metadata) => (
                    <Grid item key={metadata.key} xs={6}>
                        <Grid item>
                            <Typography
                                className={classes.fontMedium}
                                variant="body2"
                            >
                                {capitalize(metadata.key)}:
                            </Typography>
                        </Grid>

                        <Grid item>
                            <Typography variant="body2">
                                {metadata.value}
                            </Typography>
                        </Grid>
                    </Grid>
                ))}
        </Grid>
    );
};
