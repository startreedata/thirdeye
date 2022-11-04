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
