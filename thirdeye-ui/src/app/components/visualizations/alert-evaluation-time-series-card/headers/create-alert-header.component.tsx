// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Box, Button, CardHeader, Grid } from "@material-ui/core";
import React, { FunctionComponent, useCallback } from "react";
import { TimeRangeButtonWithContext } from "../../../time-range/time-range-button-with-context/time-range-button.component";
import { CreateAlertHeaderProps } from "../alert-evaluation-time-series-card.interfaces";

export const CreateAlertHeader: FunctionComponent<CreateAlertHeaderProps> = ({
    title,
    onRefresh,
}) => {
    const handlePreview = useCallback(() => {
        onRefresh && onRefresh();
    }, [onRefresh]);

    return (
        <CardHeader
            action={
                <Grid container>
                    <Grid item>
                        <TimeRangeButtonWithContext
                            onTimeRangeChange={(start: number, end: number) =>
                                onRefresh && onRefresh(start, end)
                            }
                        />
                    </Grid>

                    <Grid item>
                        <Box>
                            <Button
                                color="primary"
                                variant="contained"
                                onClick={handlePreview}
                            >
                                Preview
                            </Button>
                        </Box>
                    </Grid>
                </Grid>
            }
            title={title}
            titleTypographyProps={{ variant: "h6" }}
        />
    );
};
