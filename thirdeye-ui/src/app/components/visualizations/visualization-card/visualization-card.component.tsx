import {
    Card,
    CardContent,
    CardHeader,
    FormHelperText,
    Grid,
    IconButton,
} from "@material-ui/core";
import FullscreenExitIcon from "@material-ui/icons/FullscreenExit";
import RefreshIcon from "@material-ui/icons/Refresh";
import classnames from "classnames";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useCommonStyles } from "../../../utils/material-ui/common.styles";
import { VisualizationCardProps } from "./visualization-card.interfaces";
import { useVisualizationCardStyles } from "./visualization-card.styles";

export const VisualizationCard: FunctionComponent<VisualizationCardProps> = (
    props: VisualizationCardProps
) => {
    const visualizationCardClasses = useVisualizationCardStyles(props);
    const commonClasses = useCommonStyles();
    const [maximized, setMaximized] = useState(props.maximized);

    useEffect(() => {
        // Maximize/restore input changed, update
        setMaximized(props.maximized);
    }, [props.maximized]);

    useEffect(() => {
        // Maximize/restore state changed, notify
        if (maximized) {
            props.onMaximize && props.onMaximize();
        }

        if (!maximized) {
            props.onRestore && props.onRestore();
        }
    }, [maximized]);

    useEffect(() => {
        addDocumentKeyDownListener();

        return removeDocumentKeyDownListener;
    }, []);

    const addDocumentKeyDownListener = (): void => {
        document.addEventListener("keydown", handleDocumentKeyDown);
    };

    const removeDocumentKeyDownListener = (): void => {
        document.removeEventListener("keydown", handleDocumentKeyDown);
    };

    const handleDocumentKeyDown = (event: KeyboardEvent): void => {
        if (event.key === "Escape") {
            setMaximized(false);
        }
    };

    const handleVisualizationCardRestore = (): void => {
        setMaximized(false);
    };

    return (
        <>
            {/* Visualization card */}
            <Card
                className={
                    maximized
                        ? visualizationCardClasses.visualizationCardMaximized
                        : visualizationCardClasses.visualizationCard
                }
                elevation={maximized ? 24 : 0} // Same as Material-UI dialog when maximized
                variant="elevation"
            >
                {maximized && (
                    <CardHeader
                        action={
                            <Grid container alignItems="center" spacing={0}>
                                {/* Helper text */}
                                {props.helperText && (
                                    <Grid item>
                                        <FormHelperText
                                            className={
                                                visualizationCardClasses.helperText
                                            }
                                            error={props.error}
                                        >
                                            {props.helperText}
                                        </FormHelperText>
                                    </Grid>
                                )}

                                {/* Refresh button */}
                                {!props.hideRefreshButton && (
                                    <Grid item>
                                        <IconButton onClick={props.onRefresh}>
                                            <RefreshIcon />
                                        </IconButton>
                                    </Grid>
                                )}

                                {/* Restore button */}
                                <Grid item>
                                    <IconButton
                                        onClick={handleVisualizationCardRestore}
                                    >
                                        <FullscreenExitIcon />
                                    </IconButton>
                                </Grid>
                            </Grid>
                        }
                        className={
                            visualizationCardClasses.visualizationCardHeader
                        }
                        title={props.title}
                        titleTypographyProps={{ variant: "h6" }}
                    />
                )}

                <CardContent
                    className={classnames({
                        [visualizationCardClasses.visualizationCardContentsMaximized]: maximized,
                        [visualizationCardClasses.visualizationCardContents]: !maximized,
                        [commonClasses.cardContentBottomPaddingRemoved]: !maximized,
                    })}
                >
                    {props.children}
                </CardContent>
            </Card>

            {/* Placeholder while the visualization card is maximized */}
            {maximized && (
                <div className={visualizationCardClasses.visualizationCard} />
            )}
        </>
    );
};
