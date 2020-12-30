import { Box, Toolbar } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { Dimension } from "../utils/material-ui-util/dimension-util";
import { Palette } from "../utils/material-ui-util/palette-util";

export const AppToolbarPrototype: FunctionComponent = () => {
    return (
        <Box
            border={Dimension.WIDTH_BORDER_DEFAULT}
            borderColor={Palette.COLOR_BORDER_DEFAULT}
            borderLeft={0}
            borderRight={0}
            borderTop={0}
        >
            <Toolbar
                style={{
                    backgroundColor: `${Palette.COLOR_BACKGROUND_ALERT_ERROR}`,
                }}
                variant="dense"
            />
        </Box>
    );
};
