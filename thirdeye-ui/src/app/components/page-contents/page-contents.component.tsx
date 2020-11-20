import { Grid, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { PageContentsProps } from "./page-contents.interfaces";
import { usePageContentsStyles } from "./page-contents.style";

export const PageContents: FunctionComponent<PageContentsProps> = (
    props: PageContentsProps
) => {
    const pageContentsClasses = usePageContentsStyles();

    return (
        <main
            className={classnames(
                pageContentsClasses.container,
                props.centerAlign
                    ? pageContentsClasses.centeredContainer
                    : pageContentsClasses.expandedContainer
            )}
        >
            <Grid container direction="column">
                {/* Header, only if title is provided and/or time range is to be displayed */}
                {(props.title || !props.hideTimeRange) && (
                    <Grid
                        container
                        item
                        alignItems="center"
                        className={pageContentsClasses.header}
                        justify="space-between"
                    >
                        {/* Title */}
                        <Grid
                            item
                            className={
                                props.titleCenterAlign
                                    ? pageContentsClasses.titleCenterAlign
                                    : ""
                            }
                        >
                            <Typography variant="h5">{props.title}</Typography>
                        </Grid>

                        {/* Time range */}
                        {!props.hideTimeRange && (
                            <Grid item>
                                <Typography variant="h5">
                                    time range placeholder
                                </Typography>
                            </Grid>
                        )}
                    </Grid>
                )}

                {/* Contents */}
                <Grid item>
                    {/* Include children */}
                    {props.children}
                </Grid>
            </Grid>
        </main>
    );
};
