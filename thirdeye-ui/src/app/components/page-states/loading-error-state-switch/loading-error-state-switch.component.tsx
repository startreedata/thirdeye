// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Box, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitchProps } from "./loading-error-state-switch.interfaces";

export const LoadingErrorStateSwitch: FunctionComponent<LoadingErrorStateSwitchProps> =
    ({ isError, isLoading, errorState, loadingState, children }) => {
        return (
            <>
                {isError && (
                    <>
                        {errorState ?? (
                            <Grid xs={12}>
                                <PageContentsCardV1>
                                    <Box pb={20} pt={20}>
                                        <NoDataIndicator />
                                    </Box>
                                </PageContentsCardV1>
                            </Grid>
                        )}
                    </>
                )}
                {!isError && isLoading && (
                    <>
                        {loadingState ?? (
                            <Grid item xs={12}>
                                <PageContentsCardV1>
                                    <SkeletonV1 animation="pulse" />
                                    <SkeletonV1 animation="pulse" />
                                    <SkeletonV1 animation="pulse" />
                                </PageContentsCardV1>
                            </Grid>
                        )}
                    </>
                )}
                {!isError && !isLoading && children}
            </>
        );
    };
