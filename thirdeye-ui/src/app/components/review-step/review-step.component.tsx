import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "../button/button.component";
import CommonCodeMirror from "../editor/code-mirror.component";
import { ReviewStepProps } from "./review-step.interfaces";

export const ReviewStep: FunctionComponent<ReviewStepProps> = ({
    detectionConfig,
    subscriptionConfig,
    subscriptionGroup,
}: ReviewStepProps) => {
    const { t } = useTranslation();

    return (
        <Grid container>
            <Grid item xs={12}>
                <Typography variant="h4">{t("label.review-submit")}</Typography>
            </Grid>
            <Grid item xs={12}>
                <Typography variant="h6">
                    {t("label.detection-configuration")}
                </Typography>
            </Grid>
            <Grid item xs={12}>
                <CommonCodeMirror
                    options={{
                        mode: "text/x-ymal",
                        readOnly: true,
                    }}
                    value={detectionConfig}
                />
            </Grid>
            <Grid item xs={12}>
                <Typography variant="h6">
                    {t("label.subscription-configuration")}
                </Typography>
            </Grid>
            <Grid item xs={12}>
                <Typography variant="subtitle1">
                    {t("label.add-subscription-group")}
                </Typography>
            </Grid>
            <Grid item xs={12}>
                <Typography variant="body1">{subscriptionGroup}</Typography>
            </Grid>
            <Grid item xs={12}>
                <CommonCodeMirror
                    options={{
                        mode: "text/x-ymal",
                        readOnly: true,
                    }}
                    value={subscriptionConfig}
                />
            </Grid>
            <Grid item xs={12}>
                <Button color="primary" variant="text">
                    {t("label.preview-alert")}
                </Button>
            </Grid>
        </Grid>
    );
};
