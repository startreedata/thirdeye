import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { TimeRangeSelectorControlsProps } from "./time-range-selector-controls.interfaces";

export const TimeRangeSelectorControls: FunctionComponent<TimeRangeSelectorControlsProps> = (
    props: TimeRangeSelectorControlsProps
) => {
    const { t } = useTranslation();

    return (
        <Grid container>
            {/* Cancel button */}
            <Grid item>
                <Button
                    color="primary"
                    variant="outlined"
                    onClick={props.onCancel}
                >
                    {t("label.cancel")}
                </Button>
            </Grid>

            {/* Apply button */}
            <Grid item>
                <Button
                    color="primary"
                    variant="contained"
                    onClick={props.onApply}
                >
                    {t("label.apply")}
                </Button>
            </Grid>
        </Grid>
    );
};
