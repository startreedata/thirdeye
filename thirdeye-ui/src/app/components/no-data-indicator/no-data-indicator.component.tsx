import { Typography, useTheme } from "@material-ui/core";
import classnames from "classnames";
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
            <div
                className={classnames({
                    [noDataIndicatorClasses.icon]: !props.text,
                    [noDataIndicatorClasses.iconWithText]: props.text,
                })}
            >
                <EmptyGlassIcon
                    fill={theme.palette.primary.main}
                    height={HEIGHT_ICON}
                />
            </div>

            {/* Text */}
            {props.text && (
                <div className={noDataIndicatorClasses.text}>
                    <Typography variant="body2">{props.text}</Typography>
                </div>
            )}
        </div>
    );
};
