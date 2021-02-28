import { Typography } from "@material-ui/core";
import ErrorOutlineIcon from "@material-ui/icons/ErrorOutline";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { ErrorIndicatorProps } from "./error-indicator.interfaces";
import { useErrorIndicatorStyles } from "./error-indicator.styles";

export const ErrorIndicator: FunctionComponent<ErrorIndicatorProps> = (
    props: ErrorIndicatorProps
) => {
    const errorIndicatorClasses = useErrorIndicatorStyles();
    const { t } = useTranslation();

    return (
        <div className={errorIndicatorClasses.errorIndicator}>
            {/* Icon */}
            <div className={errorIndicatorClasses.icon}>
                <ErrorOutlineIcon color="error" fontSize="large" />
            </div>

            {/* Text */}
            <div className={errorIndicatorClasses.text}>
                <Typography variant="body2">
                    {props.text || t("message.error")}
                </Typography>
            </div>
        </div>
    );
};
