import { makeStyles } from "@material-ui/core";

export const useLinearAxisYStyles = makeStyles((theme) => ({
    tick: {
        ...theme.typography.overline,
    },
}));
