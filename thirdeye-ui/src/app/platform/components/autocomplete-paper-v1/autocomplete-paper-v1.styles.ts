import { makeStyles } from "@material-ui/core";
import { DimensionV1 } from "../../utils/material-ui/dimension.util";

export const useAutocompletePaperV1Styles = makeStyles({
    autocompletePaper: {
        borderRadius: DimensionV1.PopoverBorderRadius,
    },
});
