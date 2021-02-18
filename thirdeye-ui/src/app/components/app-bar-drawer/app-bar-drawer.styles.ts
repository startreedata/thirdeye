import { makeStyles } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui/dimension.util";

export const useAppBarDrawerStyles = makeStyles((theme) => ({
    appBarDrawerPaper: {
        width: `${Dimension.WIDTH_DRAWER_DEFAULT}px`,
    },
    listItemNested: {
        paddingLeft: theme.spacing(4),
    },
}));
