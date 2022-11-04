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
