import {
    ClickAwayListener,
    Drawer,
    IconButton,
    Toolbar,
    useMediaQuery,
    useTheme,
} from "@material-ui/core";
import { classnames } from "@material-ui/data-grid";
import ChevronRightIcon from "@material-ui/icons/ChevronRight";
import React, { FunctionComponent, useEffect, useState } from "react";
import { AppDrawerProps } from "./app-drawer.interfaces";
import { useAppDrawerStyles } from "./app-drawer.styles";

export const ID_APP_DRAWER = "ID_APP_DRAWER";

// In order to work well with PageContents, app drawer has to be a top level child in PageContents
export const AppDrawer: FunctionComponent<AppDrawerProps> = (
    props: AppDrawerProps
) => {
    const appDrawerClasses = useAppDrawerStyles();
    const [appDrawerOpen, setAppDrawerOpen] = useState(true);
    const theme = useTheme();
    const screenWidthSmDown = useMediaQuery(theme.breakpoints.down("sm"));

    useEffect(() => {
        // Screen width changed, minimize/restore app drawer
        setAppDrawerOpen(!screenWidthSmDown);
    }, [screenWidthSmDown]);

    const handleAppDrawerExpand = (): void => {
        setAppDrawerOpen(true);
    };

    const handleClickAway = (): void => {
        setAppDrawerOpen(!screenWidthSmDown);
    };

    return (
        <div id={ID_APP_DRAWER}>
            <ClickAwayListener onClickAway={handleClickAway}>
                <Drawer
                    classes={{
                        paper: classnames({
                            [appDrawerClasses.drawerPaper]: appDrawerOpen,
                            [appDrawerClasses.drawerPaperMinimized]: !appDrawerOpen,
                        }),
                    }}
                    variant="permanent"
                >
                    {/* Required to clip the container under app bar */}
                    <Toolbar />

                    {/* Expand button */}
                    {!appDrawerOpen && (
                        <IconButton onClick={handleAppDrawerExpand}>
                            <ChevronRightIcon />
                        </IconButton>
                    )}

                    {/* Contents */}
                    {appDrawerOpen && (
                        <div className={appDrawerClasses.appDrawerContents}>
                            {props.children}
                        </div>
                    )}
                </Drawer>
            </ClickAwayListener>
        </div>
    );
};
