import { LinkProps } from "@material-ui/core";

// Material-UI theme style overrides for Link
export const linkClasses = {
    root: {
        cursor: "pointer",
    },
};

// Material-UI theme property overrides for Link
export const linkProps: Partial<LinkProps> = {
    underline: "none",
    rel: "noopener",
};
