// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { InputSectionProps } from "./input-section.interfaces";

export const InputSection: FunctionComponent<InputSectionProps> = ({
    label,
    helperLabel,
    labelComponent,
    inputComponent,
    fullWidth,
}) => {
    return (
        <Grid container item alignItems="center" xs={12}>
            <Grid item lg={2} md={4} sm={12} xs={12}>
                {!!labelComponent && labelComponent}
                {!labelComponent && label && (
                    <>
                        <Typography variant="body2">{label}</Typography>
                        {helperLabel && (
                            <Typography variant="caption">
                                {helperLabel}
                            </Typography>
                        )}
                    </>
                )}
            </Grid>
            <Grid
                item
                lg={fullWidth ? 10 : 4}
                md={fullWidth ? 8 : 5}
                sm={12}
                xs={12}
            >
                {inputComponent}
            </Grid>
        </Grid>
    );
};
