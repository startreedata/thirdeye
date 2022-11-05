import { TabsProps } from "@material-ui/core";
import { BorderV1 } from "./border.util";
import { DimensionV1 } from "./dimension.util";
import { typographyOptionsV1 } from "./typography.util";

// Material UI theme style overrides for Tabs
export const tabsClassesV1 = {
    root: {
        minHeight: DimensionV1.TabHeight,
        maxHeight: DimensionV1.TabHeight,
    },
};

export const tabClassesV1 = {
    root: {
        minHeight: DimensionV1.TabHeight,
        maxHeight: DimensionV1.TabHeight,
        maxWidth: "none",
        alignItems: "flex-start",
        "text-transform": "none",
        ...typographyOptionsV1.subtitle1,
        paddingTop: 4,
        "&$selected": {
            ...typographyOptionsV1.h6,
            borderBottom: BorderV1.TabBorderSelected,
        },
    },
    textColorInherit: {
        opacity: 1,
    },
};

// Material UI theme property overrides for Tabs
export const tabsPropsV1: Partial<TabsProps> = {
    scrollButtons: "off",
    TabIndicatorProps: {
        hidden: true, // Default selected tab indicator is not responsive
    },
    variant: "scrollable",
};
