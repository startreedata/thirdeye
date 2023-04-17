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
import { Box, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    SkeletonV1,
} from "../../../platform/components";

export const PageSkeleton: FunctionComponent = () => {
    return (
        <PageV1>
            <PageContentsGridV1>
                {/** header section */}
                <Grid item xs={12}>
                    <Box paddingTop={2}>
                        <SkeletonV1 height={36} />
                        <SkeletonV1 height={80} />
                    </Box>
                </Grid>

                {/** information card section */}
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <SkeletonV1 height={45} />
                    </PageContentsCardV1>
                </Grid>

                {/** confirm anomaly section */}
                <Grid item xs={12}>
                    <SkeletonV1 height={72} />
                </Grid>

                {/** chart section */}
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <SkeletonV1 height={50} />
                        <SkeletonV1 height={370} />
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
