import { makeStyles, Theme } from "@material-ui/core";
import { BreadcrumbsProps } from "./breadcrumbs.interfaces";

export const useBreadcrumbsStyles = makeStyles<Theme, BreadcrumbsProps>({
    maxWidthBreadcrumb: {
        maxWidth: (props) => props.breadcrumbMaxWidth,
    },
});
