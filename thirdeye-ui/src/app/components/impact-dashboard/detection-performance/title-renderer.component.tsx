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
import React from "react";
import { Grid, Typography } from "@material-ui/core";
import HelpOutlineIcon from "@material-ui/icons/HelpOutline";
import { TileRendererProps } from "./detection-performance.interfaces";

export const TitleRenderer = ({
    title,
    notificationText,
}: TileRendererProps): JSX.Element => {
    return (
        <Grid container alignItems="center" direction="row" xs={12}>
            <Grid item>
                <Typography variant="h6">{title}</Typography>
            </Grid>
            {notificationText && (
                <Grid item alignItems="center" direction="row">
                    <HelpOutlineIcon />
                </Grid>
            )}
        </Grid>
    );
};
