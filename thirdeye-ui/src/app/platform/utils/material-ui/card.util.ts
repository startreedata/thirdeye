import { CardProps } from "@material-ui/core";
import { DimensionV1 } from "./dimension.util";

// Material UI theme style overrides for Card
export const cardClassesV1 = {
    root: {
        borderRadius: DimensionV1.CardBorderRadius,
    },
};

export const cardContentClassesV1 = {
    root: {
        padding: DimensionV1.CardContentPadding,
        "&:last-child": {
            paddingBottom: DimensionV1.CardContentPadding,
        },
    },
};

// Material UI theme property overrides for Card
export const cardPropsV1: Partial<CardProps> = {
    elevation: 0,
};
