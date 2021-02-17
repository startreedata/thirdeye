import {
    Card,
    CardContent,
    CardHeader,
    Fade,
    FormHelperText,
    Grid,
    IconButton,
} from "@material-ui/core";
import FullscreenIcon from "@material-ui/icons/Fullscreen";
import FullscreenExitIcon from "@material-ui/icons/FullscreenExit";
import RefreshIcon from "@material-ui/icons/Refresh";
import classnames from "classnames";
import React, { FunctionComponent, useEffect, useState } from "react";
import { Flipped, Flipper } from "react-flip-toolkit";
import { useCommonStyles } from "../../../utils/material-ui/common.styles";
import { VisualizationCardProps } from "./visualization-card.interfaces";
import { useVisualizationCardStyles } from "./visualization-card.styles";

export const VisualizationCard: FunctionComponent<VisualizationCardProps> = (
    props: VisualizationCardProps
) => {
    const visualizationCardClasses = useVisualizationCardStyles(props);
    const commonClasses = useCommonStyles();
    const [maximized, setMaximized] = useState(props.startMaximized);
    const [backdrop, setBackdrop] = useState(props.startMaximized);

    useEffect(() => {
        addDocumentKeyDownListener();

        return removeDocumentKeyDownListener;
    }, []);

    const addDocumentKeyDownListener = (): void => {
        document.addEventListener("keydown", handleKeyDown);
    };

    const removeDocumentKeyDownListener = (): void => {
        document.removeEventListener("keydown", handleKeyDown);
    };

    const handleMaximizeToggle = (): void => {
        setMaximized((maximized) => !maximized);
    };

    const handleMaximizeToggleStart = (): void => {
        // If this is the beginning of maximize animation, turn on backdrop
        // Else, don't change the backdrop
        if (maximized) {
            setBackdrop(true);
        }
    };

    const handleMaximizeToggleComplete = (): void => {
        // If this is the end of restore animation, turn off backdrop
        // Else, don't change the backdrop
        if (!maximized) {
            setBackdrop(false);
        }
    };

    const handleKeyDown = (event: KeyboardEvent): void => {
        if (event.key === "Escape") {
            setMaximized(false);
        }
    };

    return (
        <>
            {/* Visualization card */}
            <Flipper
                flipKey={maximized}
                onComplete={handleMaximizeToggleComplete}
                onStart={handleMaximizeToggleStart}
            >
                <Flipped flipId="visualizationCard">
                    <Card
                        className={
                            maximized
                                ? visualizationCardClasses.cardMaximized
                                : visualizationCardClasses.card
                        }
                        elevation={24} // Same as Material-UI dialog
                        variant={maximized ? "elevation" : "outlined"}
                    >
                        <CardHeader
                            action={
                                <Grid container alignItems="center" spacing={0}>
                                    {/* Stale label */}
                                    {props.stale && (
                                        <Grid item>
                                            <FormHelperText error>
                                                {props.staleLabel}
                                            </FormHelperText>
                                        </Grid>
                                    )}

                                    {/* Refresh button */}
                                    {props.showRefreshButton && (
                                        <Grid item>
                                            <IconButton
                                                onClick={props.onRefresh}
                                            >
                                                <RefreshIcon />
                                            </IconButton>
                                        </Grid>
                                    )}

                                    {/* Maximize/restore button */}
                                    {props.showMaximizeButton && (
                                        <Grid item>
                                            <IconButton
                                                onClick={handleMaximizeToggle}
                                            >
                                                {/* Maximize button */}
                                                {!maximized && (
                                                    <FullscreenIcon />
                                                )}

                                                {/* Restore button */}
                                                {maximized && (
                                                    <FullscreenExitIcon />
                                                )}
                                            </IconButton>
                                        </Grid>
                                    )}
                                </Grid>
                            }
                            className={visualizationCardClasses.header}
                            title={
                                maximized
                                    ? props.maximizedTitle || props.title
                                    : props.title
                            }
                            titleTypographyProps={{ variant: "h6" }}
                        />

                        <CardContent
                            className={visualizationCardClasses.contents}
                        >
                            {props.children}
                        </CardContent>
                    </Card>
                </Flipped>
            </Flipper>

            {/* Placeholder while the visualization card is maximized */}
            {maximized && <div className={visualizationCardClasses.card} />}

            {/* Backdrop */}
            <Fade in={backdrop}>
                <div
                    className={classnames(
                        visualizationCardClasses.backdrop,
                        commonClasses.backdrop
                    )}
                />
            </Fade>
        </>
    );
};
