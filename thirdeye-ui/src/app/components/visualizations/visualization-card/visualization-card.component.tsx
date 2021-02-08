import {
    Card,
    CardContent,
    CardHeader,
    Fade,
    FormHelperText,
    Grid,
    IconButton,
    Typography,
} from "@material-ui/core";
import { Fullscreen, FullscreenExit, Refresh } from "@material-ui/icons";
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
        addDocumentListener();

        return removeDocumentListener;
    }, []);

    const onMaximizeToggle = (): void => {
        setMaximized((maximized) => !maximized);
    };

    const onMaximizeToggleStart = (): void => {
        // If this is the beginning of maximize animation, turn on backdrop
        // Else, don't change the backdrop
        if (maximized) {
            setBackdrop(true);
        }
    };

    const onMaximizeToggleComplete = (): void => {
        // If this is the end of restore animation, turn off backdrop
        // Else, don't change the backdrop
        if (!maximized) {
            setBackdrop(false);
        }
    };

    const onKeyDown = (event: KeyboardEvent): void => {
        if (event.key === "Escape") {
            setMaximized(false);
        }
    };

    const addDocumentListener = (): void => {
        document.addEventListener("keydown", onKeyDown);
    };

    const removeDocumentListener = (): void => {
        document.removeEventListener("keydown", onKeyDown);
    };

    return (
        <div>
            <Flipper
                flipKey={maximized}
                onComplete={onMaximizeToggleComplete}
                onStart={onMaximizeToggleStart}
            >
                <Flipped flipId="visualizationCard">
                    <Card
                        className={
                            maximized
                                ? visualizationCardClasses.outerContainerMaximize
                                : visualizationCardClasses.outerContainer
                        }
                        elevation={24} // Same as Material-UI dialog
                        variant={maximized ? "elevation" : "outlined"}
                    >
                        {/* Visualization card */}
                        <CardHeader
                            disableTypography
                            action={
                                <Grid container alignItems="center" spacing={0}>
                                    {/* Stale label */}
                                    <Grid item>
                                        <FormHelperText error>
                                            {props.staleLabel || ""}
                                        </FormHelperText>
                                    </Grid>

                                    {/* Refresh button */}
                                    {props.showRefreshButton && (
                                        <Grid item>
                                            <IconButton
                                                onClick={props.onRefresh}
                                            >
                                                <Refresh />
                                            </IconButton>
                                        </Grid>
                                    )}

                                    {/* Maximize/restore button */}
                                    {props.showMaximizeButton && (
                                        <Grid item>
                                            <IconButton
                                                onClick={onMaximizeToggle}
                                            >
                                                {/* Maximize button */}
                                                {!maximized && <Fullscreen />}

                                                {/* Restore button */}
                                                {maximized && (
                                                    <FullscreenExit />
                                                )}
                                            </IconButton>
                                        </Grid>
                                    )}
                                </Grid>
                            }
                            className={visualizationCardClasses.headerContainer}
                            title={
                                <>
                                    {/* Title */}
                                    {!maximized && (
                                        <Typography variant="h6">
                                            {props.title || ""}
                                        </Typography>
                                    )}

                                    {/* Maximized title */}
                                    {maximized && (
                                        <Typography variant="h6">
                                            {props.maximizedTitle ||
                                                props.title ||
                                                ""}
                                        </Typography>
                                    )}
                                </>
                            }
                        />

                        <CardContent
                            className={visualizationCardClasses.innerContainer}
                        >
                            {/* Contents */}
                            {props.children}
                        </CardContent>
                    </Card>
                </Flipped>
            </Flipper>

            {/* Placeholder while the visualization card is maximized */}
            {maximized && (
                <div className={visualizationCardClasses.outerContainer} />
            )}

            {/* Backdrop */}
            <Fade in={backdrop}>
                <div
                    className={classnames(
                        visualizationCardClasses.backdrop,
                        commonClasses.backdrop
                    )}
                />
            </Fade>
        </div>
    );
};
