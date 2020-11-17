import { AppBar, Link, Toolbar } from "@material-ui/core";
import AddIcon from "@material-ui/icons/Add";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { Link as RouterLink } from "react-router-dom";
import { ReactComponent as ThirdEye } from "../../../assets/icons/third-eye.svg";
import { AppRoute } from "../../utils/route/routes.util";
import { Button } from "../button/button.component";
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

                <RouterLink
                    style={{ textDecoration: "none" }}
                    to={AppRoute.ALERTS_CREATE}
                >
                    <Button
                        color="primary"
                        startIcon={<AddIcon />}
                        variant="text"
                    >
                        Create Alert
                    </Button>
                </RouterLink>
            </Toolbar>
        </AppBar>
    );
};
