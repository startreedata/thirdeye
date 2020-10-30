import { AppBar, Link, Toolbar } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { ReactComponent as ThirdEye } from "../../../assets/icons/third-eye.svg";
import { applicationBarStyles } from "./application-bar.styles";

// ThirdEye UI AppBar
export const ApplicationBar: FunctionComponent = () => {
    const applicationBarClasses = applicationBarStyles();

    const onLogoClick = (): void => {
        // Empty
    };

    const onAlertsClick = (): void => {
        // Empty
    };

    return (
        <AppBar className={applicationBarClasses.applicationBar}>
            {/* Required to appropriately layout children in AppBar */}
            <Toolbar>
                {/* ThirdEye logo */}
                <Link
                    className={applicationBarClasses.logo}
                    component="button"
                    onClick={onLogoClick}
                >
                    <ThirdEye width={48} />
                </Link>
                {/* Alerts */}
                <Link
                    className={classnames(
                        applicationBarClasses.link,
                        applicationBarClasses.linkLeftAlign
                    )}
                    component="button"
                    variant="subtitle2"
                    onClick={onAlertsClick}
                >
                    Alerts
                </Link>
            </Toolbar>
        </AppBar>
    );
};
