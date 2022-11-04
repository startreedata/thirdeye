// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { SafariMuiGridFix } from "../../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { TimeRangeSelectorControlsProps } from "./time-range-selector-controls.interfaces";

export const TimeRangeSelectorControls: FunctionComponent<TimeRangeSelectorControlsProps> =
    (props: TimeRangeSelectorControlsProps) => {
        const { t } = useTranslation();

        return (
            <Grid container spacing={1}>
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

                {/* Fixes layout in Safari */}
                <SafariMuiGridFix />
            </Grid>
        );
    };
