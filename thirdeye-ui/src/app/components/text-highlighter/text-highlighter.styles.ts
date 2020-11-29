import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui/palette-util";

export const useTextHighlighterStyles = makeStyles((theme: Theme) => {
    return createStyles({
        highlight: {
            color: Palette.COLOR_TEXT_DEFAULT,
            backgroundColor: theme.palette.primary.main,
        },
    });
});
