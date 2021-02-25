import { Box, CircularProgress } from "@material-ui/core";
import React, { FunctionComponent } from "react";

export const LoadingIndicator: FunctionComponent = () => {
    return (
        <Box
            alignItems="center"
            display="flex"
            flex={1}
            height="100%"
            justifyContent="center"
            width="100%"
        >
            {/* Loading indicator */}
            <CircularProgress color="primary" />
        </Box>
    );
};
