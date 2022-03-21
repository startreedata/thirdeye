import { makeStyles } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui/dimension.util";

export const useAppBarDrawerStyles = makeStyles((theme) => ({
    drawerPaper: {
        width: Dimension.WIDTH_DRAWER_DEFAULT,
    },
    listItemNested: {
        paddingLeft: theme.spacing(4),
    },
}));
