/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
There is a component similar to this but that is scoped to alert-wizard-v2 component.
Since this is a general purpose component we should shift that to here eventually.
Info icon should always be sitting a bit on the top side,
and should not be aligned with the rest of the element,
which is what is happening with the existing component.
*/
import React, { FunctionComponent } from "react";
import { InfoLabelProps } from "./info-label.interfaces";
import { Grid, Tooltip, Typography } from "@material-ui/core";
import InfoIconOutlined from "@material-ui/icons/InfoOutlined";

export const InfoLabel: FunctionComponent<InfoLabelProps> = ({
    label,
    tooltipText = "",
}) => {
    return (
        <Grid container alignItems="center">
            <Grid item lg={12} md={12} sm={12} xs={12}>
                <Typography variant="body2">
                    {label}
                    {tooltipText && (
                        <Tooltip
                            arrow
                            interactive
                            placement="top"
                            title={
                                <Typography variant="caption">
                                    {tooltipText}
                                </Typography>
                            }
                        >
                            <InfoIconOutlined
                                color="secondary"
                                fontSize="small"
                            />
                        </Tooltip>
                    )}
                </Typography>
            </Grid>
        </Grid>
    );
};
