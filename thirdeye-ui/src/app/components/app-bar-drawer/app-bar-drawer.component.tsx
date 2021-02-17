import {
    Drawer,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    useTheme,
} from "@material-ui/core";
import HomeIcon from "@material-ui/icons/Home";
import SettingsIcon from "@material-ui/icons/Settings";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";
import { ReactComponent as AlertIcon } from "../../../assets/images/alert.svg";
import { ReactComponent as AnomalyIcon } from "../../../assets/images/anomaly.svg";
import {
    AppRoute,
    getAlertsPath,
    getAnomaliesAllPath,
    getConfigurationPath,
    getHomePath,
} from "../../utils/routes/routes.util";
import { AppBarDrawerProps } from "./app-bar-drawer.interfaces";
import { useAppBarDrawerStyles } from "./app-bar-drawer.styles";

export const AppBarDrawer: FunctionComponent<AppBarDrawerProps> = (
    props: AppBarDrawerProps
) => {
    const appBarDrawerClasses = useAppBarDrawerStyles();
    const history = useHistory();
    const location = useLocation();
    const theme = useTheme();
    const { t } = useTranslation();

    const handleHomeClick = (): void => {
        history.push(getHomePath());
        props.onClose && props.onClose();
    };

    const handleAlertsClick = (): void => {
        history.push(getAlertsPath());
        props.onClose && props.onClose();
    };

    const handleAnomaliesClick = (): void => {
        history.push(getAnomaliesAllPath());
        props.onClose && props.onClose();
    };

    const handleConfigurationClick = (): void => {
        history.push(getConfigurationPath());
        props.onClose && props.onClose();
    };

    const isRouteCurrent = (route: string): boolean => {
        return location.pathname.indexOf(route) === 0;
    };

    return (
        <Drawer
            anchor="left"
            classes={{ paper: appBarDrawerClasses.appBarDrawerPaper }}
            open={props.open}
            onClose={props.onClose}
        >
            <List>
                {/* Home */}
                <ListItem
                    button
                    selected={isRouteCurrent(AppRoute.HOME)}
                    onClick={handleHomeClick}
                >
                    <ListItemIcon>
                        <HomeIcon color="action" />
                    </ListItemIcon>

                    <ListItemText
                        primary={t("label.home")}
                        primaryTypographyProps={{
                            variant: "subtitle1",
                            color: isRouteCurrent(AppRoute.HOME)
                                ? "textPrimary"
                                : "primary",
                        }}
                    />
                </ListItem>

                {/* Alerts */}
                <ListItem
                    button
                    selected={isRouteCurrent(AppRoute.ALERTS)}
                    onClick={handleAlertsClick}
                >
                    <ListItemIcon>
                        <AlertIcon
                            fill={theme.palette.action.active}
                            width={24}
                        />
                    </ListItemIcon>

                    <ListItemText
                        primary={t("label.alerts")}
                        primaryTypographyProps={{
                            variant: "subtitle1",
                            color: isRouteCurrent(AppRoute.ALERTS)
                                ? "textPrimary"
                                : "primary",
                        }}
                    />
                </ListItem>

                {/* Anomalies */}
                <ListItem
                    button
                    selected={isRouteCurrent(AppRoute.ANOMALIES)}
                    onClick={handleAnomaliesClick}
                >
                    <ListItemIcon>
                        <AnomalyIcon
                            fill={theme.palette.action.active}
                            width={24}
                        />
                    </ListItemIcon>

                    <ListItemText
                        primary={t("label.anomalies")}
                        primaryTypographyProps={{
                            variant: "subtitle1",
                            color: isRouteCurrent(AppRoute.ANOMALIES)
                                ? "textPrimary"
                                : "primary",
                        }}
                    />
                </ListItem>

                {/* Configuration */}
                <ListItem
                    button
                    selected={isRouteCurrent(AppRoute.CONFIGURATION)}
                    onClick={handleConfigurationClick}
                >
                    <ListItemIcon>
                        <SettingsIcon color="action" />
                    </ListItemIcon>

                    <ListItemText
                        primary={t("label.configuration")}
                        primaryTypographyProps={{
                            variant: "subtitle1",
                            color: isRouteCurrent(AppRoute.CONFIGURATION)
                                ? "textPrimary"
                                : "primary",
                        }}
                    />
                </ListItem>
            </List>
        </Drawer>
    );
};
