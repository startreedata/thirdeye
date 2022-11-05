/*
 * Copyright 2022 StarTree Inc
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
import { Grid, Typography } from "@material-ui/core";
import React from "react";
import { formatLargeNumberV1 } from "../../../platform/utils";
import { SafariMuiGridFix } from "../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { TreemapData } from "./treemap.interfaces";
import { useTreemapStyles } from "./treemap.styles";

function GenericTreemapTooltip<Data>(props: TreemapData<Data>): JSX.Element {
    const treemapStyles = useTreemapStyles();

    return (
        <Grid
            container
            className={treemapStyles.alertEvaluationTimeSeriesTooltip}
            direction="column"
            spacing={0}
        >
            {/* Name of the Dimension */}
            <Grid item className={treemapStyles.time}>
                <Grid
                    container
                    alignItems="center"
                    justifyContent="center"
                    spacing={0}
                >
                    <Grid item>
                        <Typography variant="overline">{props.id}</Typography>
                    </Grid>
                </Grid>
            </Grid>

            <Grid item className={treemapStyles.nameValueContents}>
                {/* Value */}
                <Typography variant="overline">
                    {formatLargeNumberV1(props.size)}
                </Typography>
            </Grid>

            <SafariMuiGridFix />
        </Grid>
    );
}

export { GenericTreemapTooltip };
