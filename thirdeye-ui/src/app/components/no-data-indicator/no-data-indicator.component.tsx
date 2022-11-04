import { Typography, useTheme } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { ReactComponent as EmptyGlassIcon } from "../../../assets/images/empty-glass.svg";
import { NoDataIndicatorProps } from "./no-data-indicator.interfaces";
import { useNoDataIndicatorStyles } from "./no-data-indicator.styles";

const HEIGHT_ICON = 36;

export const NoDataIndicator: FunctionComponent<NoDataIndicatorProps> = (
    props: NoDataIndicatorProps
) => {
    const noDataIndicatorClasses = useNoDataIndicatorStyles();
    const theme = useTheme();

    return (
        <div className={noDataIndicatorClasses.noDataIndicator}>
            {/* Icon */}
            <div>
                <EmptyGlassIcon
                    fill={theme.palette.primary.main}
                    height={HEIGHT_ICON}
                />
            </div>

            {/* Text */}
            {props.text && (
                <div>
                    <Typography variant="body2">{props.text}</Typography>
                </div>
            )}

            {/* children */}
            {props.children && <div>{props.children}</div>}
        </div>
    );
};
