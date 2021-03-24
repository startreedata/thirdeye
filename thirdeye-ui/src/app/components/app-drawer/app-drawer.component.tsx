import {
    ClickAwayListener,
    Drawer,
    IconButton,
    Slide,
    Toolbar,
    useMediaQuery,
    useTheme,
} from "@material-ui/core";
import ChevronRightIcon from "@material-ui/icons/ChevronRight";
import classnames from "classnames";
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
        // Input changed, minimize/restore app drawer
        setAppDrawerOpen(!screenWidthSmDown || Boolean(props.open));
    }, [props.open]);

    useEffect(() => {
        // Screen width changed, minimize/restore app drawer and update document listeners
        setAppDrawerOpen(!screenWidthSmDown);
        addDocumentKeyDownListener();

        return removeDocumentKeyDownListener;
    }, [screenWidthSmDown]);

    useEffect(() => {
        // App drawer minimized/restored, notify
        props.onChange && props.onChange(appDrawerOpen);
    }, [appDrawerOpen]);

    const addDocumentKeyDownListener = (): void => {
        document.addEventListener("keydown", handleDocumentKeyDown);
    };

    const removeDocumentKeyDownListener = (): void => {
        document.removeEventListener("keydown", handleDocumentKeyDown);
    };

    const handleDocumentKeyDown = (event: KeyboardEvent): void => {
        if (event.key === "Escape") {
            setAppDrawerOpen(!screenWidthSmDown);
        }
    };

    const handleAppDrawerRestore = (): void => {
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

                    {/* Restore button */}
                    {!appDrawerOpen && (
                        <Slide
                            direction="right"
                            in={!appDrawerOpen}
                            timeout={{
                                enter:
                                    theme.transitions.duration.enteringScreen *
                                    2,
                                exit: 0,
                            }}
                        >
                            <IconButton onClick={handleAppDrawerRestore}>
                                <ChevronRightIcon />
                            </IconButton>
                        </Slide>
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
