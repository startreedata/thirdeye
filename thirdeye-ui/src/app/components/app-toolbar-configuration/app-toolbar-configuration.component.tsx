import { Box, Link, Toolbar } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";
import { Dimension } from "../../utils/material-ui-util/dimension-util";
import { Palette } from "../../utils/material-ui-util/palette-util";
import {
    AppRoute,
    getConfigurationSubscriptionGroupsPath,
} from "../../utils/routes-util/routes-util";
import { useAppToolbarConfigurationStyles } from "./app-toolbar-configuration.styles";

export const AppToolbarConfiguration: FunctionComponent = () => {
    const appToolbarConfigurationClasses = useAppToolbarConfigurationStyles();
    const history = useHistory();
    const location = useLocation();
    const { t } = useTranslation();

    const onSubscriptionGroupsClick = (): void => {
        history.push(getConfigurationSubscriptionGroupsPath());
    };

    const isRouteCurrent = (route: string): boolean => {
        return location.pathname.indexOf(route) === 0;
    };

    return (
        <Box
            border={Dimension.WIDTH_BORDER_DEFAULT}
            borderColor={Palette.COLOR_BORDER_DEFAULT}
            borderLeft={0}
            borderRight={0}
            borderTop={0}
            className={appToolbarConfigurationClasses.container}
        >
            {/* Required to appropriately layout children in toolbar */}
            <Toolbar variant="dense">
                {/* Subscription Groups */}
                <Link
                    className={classnames(
                        appToolbarConfigurationClasses.link,
                        isRouteCurrent(
                            AppRoute.CONFIGURATION_SUBSCRIPTION_GROUPS
                        )
                            ? appToolbarConfigurationClasses.selectedLink
                            : ""
                    )}
                    component="button"
                    variant="subtitle2"
                    onClick={onSubscriptionGroupsClick}
                >
                    {t("label.subscription-groups")}
                </Link>
            </Toolbar>
        </Box>
    );
};
