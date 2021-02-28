import { Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { usePageNotFoundIndicatorStyles } from "./page-not-found-indicator.styles";

export const PageNotFoundIndicator: FunctionComponent = () => {
    const pageNotFoundIndicatorClasses = usePageNotFoundIndicatorStyles();
    const { t } = useTranslation();

    return (
        <div className={pageNotFoundIndicatorClasses.pageNotFoundIndicator}>
            {/* Error code */}
            <div className={pageNotFoundIndicatorClasses.errorCode}>
                <Typography color="primary" variant="h3">
                    {t("label.404")}
                </Typography>
            </div>

            {/* Error message */}
            <div className={pageNotFoundIndicatorClasses.errorMessage}>
                <Typography variant="body2">
                    {t("message.page-not-found")}
                </Typography>
            </div>
        </div>
    );
};
