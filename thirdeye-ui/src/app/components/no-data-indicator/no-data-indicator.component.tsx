import { Typography, useTheme } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { ReactComponent as EmptyGlassIcon } from "../../../assets/images/empty-glass.svg";
import { NoDataIndicatorProps } from "./no-data-indicator.interfaces";
import { useNoDataIndicatorStyles } from "./no-data-indicator.styles";

export const NoDataIndicator: FunctionComponent<NoDataIndicatorProps> = (
    props: NoDataIndicatorProps
) => {
    const noDataIndicatorClasses = useNoDataIndicatorStyles();
    const theme = useTheme();
    const { t } = useTranslation();

    return (
        <div className={noDataIndicatorClasses.noDataIndicator}>
            {/* Icon */}
            <div className={noDataIndicatorClasses.icon}>
                <EmptyGlassIcon fill={theme.palette.primary.main} height={36} />
            </div>

            {/* Text */}
            <div className={noDataIndicatorClasses.text}>
                <Typography variant="body2">
                    {props.text || t("message.no-data")}
                </Typography>
            </div>
        </div>
    );
};
