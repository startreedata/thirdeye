import { makeStyles, Theme } from "@material-ui/core";

export const useLinearAxisLeftStyles = makeStyles((theme: Theme) => ({
    tick: {
        ...theme.typography.overline,
    },
}));
