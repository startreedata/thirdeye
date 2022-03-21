import { Typography } from "@material-ui/core";
import ErrorOutlineIcon from "@material-ui/icons/ErrorOutline";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { ErrorIndicatorProps } from "./error-indicator.interfaces";
import { useErrorIndicatorStyles } from "./error-indicator.styles";

export const ErrorIndicator: FunctionComponent<ErrorIndicatorProps> = (
    props: ErrorIndicatorProps
) => {
    const errorIndicatorClasses = useErrorIndicatorStyles();

    return (
        <div className={errorIndicatorClasses.errorIndicator}>
            {/* Icon */}
            <div
                className={classnames({
                    [errorIndicatorClasses.icon]: !props.text,
                    [errorIndicatorClasses.iconWithText]: props.text,
                })}
            >
                <ErrorOutlineIcon color="error" fontSize="large" />
            </div>

            {/* Text */}
            {props.text && (
                <div className={errorIndicatorClasses.text}>
                    <Typography variant="body2">{props.text}</Typography>
                </div>
            )}
        </div>
    );
};
