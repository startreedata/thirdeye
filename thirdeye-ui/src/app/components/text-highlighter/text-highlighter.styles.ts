import { makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useTextHighlighterStyles = makeStyles((theme: Theme) => ({
    highlight: {
        color: Palette.COLOR_TEXT_DEFAULT,
        backgroundColor: theme.palette.primary.main,
    },
}));
