import { useTheme } from "@material-ui/core";
import FiberManualRecordIcon from "@material-ui/icons/FiberManualRecord";
import React, { FunctionComponent } from "react";
import { ActiveIndicatorProps } from "./active-indicator.interfaces";
import { useActiveIndicatorStyles } from "./active-indicator.styles";

export const ActiveIndicator: FunctionComponent<ActiveIndicatorProps> = (
    props: ActiveIndicatorProps
) => {
    const activeIndicatorClasses = useActiveIndicatorStyles();
    const theme = useTheme();

    const getIndicatorColor = (): string => {
        if (props.active) {
            return theme.palette.success.main;
        }

        return theme.palette.error.main;
    };

    return (
        <span className={activeIndicatorClasses.indicator}>
            <FiberManualRecordIcon
                fontSize="small"
                htmlColor={getIndicatorColor()}
            />
        </span>
    );
};
