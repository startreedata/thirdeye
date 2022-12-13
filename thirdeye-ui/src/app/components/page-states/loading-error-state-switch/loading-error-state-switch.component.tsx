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
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitchProps } from "./loading-error-state-switch.interfaces";

const DEFAULT_ERROR_ELEMENT = (
    <Box pb={20} pt={20}>
        <NoDataIndicator />
    </Box>
);

const DEFAULT_LOADING_ELEMENT = (
    <>
        <SkeletonV1 animation="pulse" />
        <SkeletonV1 animation="pulse" />
        <SkeletonV1 animation="pulse" />
    </>
);

export const LoadingErrorStateSwitch: FunctionComponent<LoadingErrorStateSwitchProps> =
    ({
        isError,
        isLoading,
        errorState,
        loadingState,
        children,
        wrapInGrid,
        wrapInCard,
    }) => {
        if (isError) {
            if (errorState) {
                return errorState;
            }

            if (wrapInGrid && wrapInCard) {
                return (
                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            {DEFAULT_ERROR_ELEMENT}
                        </PageContentsCardV1>
                    </Grid>
                );
            }

            if (wrapInGrid) {
                return (
                    <Grid item xs={12}>
                        {DEFAULT_ERROR_ELEMENT}
                    </Grid>
                );
            }

            return DEFAULT_ERROR_ELEMENT;
        }

        if (isLoading) {
            if (loadingState) {
                return loadingState;
            }

            if (wrapInGrid && wrapInCard) {
                return (
                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            {DEFAULT_LOADING_ELEMENT}
                        </PageContentsCardV1>
                    </Grid>
                );
            }

            if (wrapInGrid) {
                return (
                    <Grid item xs={12}>
                        {DEFAULT_LOADING_ELEMENT}
                    </Grid>
                );
            }

            return DEFAULT_LOADING_ELEMENT;
        }

        return <>{children}</>;
    };
