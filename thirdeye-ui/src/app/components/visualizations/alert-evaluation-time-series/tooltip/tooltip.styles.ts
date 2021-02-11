import { makeStyles } from "@material-ui/core";
import { Palette } from "../../../../utils/material-ui/palette.util";

export const useTooltipStyles = makeStyles((theme) => {
    return {
        tooltipContainer: {
            position: "relative",
        },
        tooltip: {
            color: `${theme.palette.primary.contrastText} !important`,
            backgroundColor: `${Palette.COLOR_BACKGROUND_TOOLTIP} !important`,
        },
        header: {
            marginBottom: "10px",
        },
    };
});
