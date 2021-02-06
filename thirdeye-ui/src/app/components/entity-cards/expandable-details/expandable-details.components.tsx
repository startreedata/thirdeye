import { Grid, Link, Typography } from "@material-ui/core";
import { ExpandLess, ExpandMore } from "@material-ui/icons";
import { isEmpty } from "lodash";
import React, { Fragment, ReactElement } from "react";
import { useTranslation } from "react-i18next";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { ExpandableDetailsProps } from "./expandable-details.interfaces";

export function ExpandableDetails<T>(
    props: ExpandableDetailsProps<T>
): ReactElement {
    const { t } = useTranslation();

    const onExpandToggle = (): void => {
        props.onChange && props.onChange(!props.expand);
    };

    return (
        <Grid container direction="column" spacing={0}>
            <Grid item>
                <Grid container spacing={0}>
                    {/* Label */}
                    <Grid item>
                        <Typography variant="subtitle2">
                            {props.label}
                        </Typography>
                    </Grid>

                    {/* Expand/collapse button */}
                    <Grid item>
                        {props.values && props.values.length > 1 && (
                            <Link component="button" onClick={onExpandToggle}>
                                {/* Expand */}
                                {!props.expand && (
                                    <ExpandMore
                                        color="primary"
                                        fontSize="small"
                                    />
                                )}

                                {/* Collapse */}
                                {props.expand && (
                                    <ExpandLess
                                        color="primary"
                                        fontSize="small"
                                    />
                                )}
                            </Link>
                        )}
                    </Grid>
                </Grid>
            </Grid>

            <Grid item>
                {/* No data available */}
                {isEmpty(props.values) && (
                    <Typography variant="body2">
                        <TextHighlighter
                            searchWords={props.searchWords}
                            text={t("label.no-data-marker")}
                        />
                    </Typography>
                )}

                {/* All values */}
                {!isEmpty(props.values) && props.expand && (
                    <>
                        {props.values.map((value, index) => (
                            <Fragment key={index}>
                                {/* Render as link */}
                                {props.link && (
                                    <Link
                                        component="button"
                                        display="block"
                                        variant="body2"
                                        onClick={(): void => {
                                            props.onLinkClick &&
                                                props.onLinkClick(value);
                                        }}
                                    >
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={props.valueTextFn(value)}
                                        />
                                    </Link>
                                )}

                                {/* Render as text */}
                                {!props.link && (
                                    <Typography variant="body2">
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={props.valueTextFn(value)}
                                        />
                                    </Typography>
                                )}
                            </Fragment>
                        ))}
                    </>
                )}

                {/* First value */}
                {!isEmpty(props.values) && !props.expand && (
                    <>
                        {/* Render as link */}
                        {props.link && (
                            <Link
                                component="button"
                                display="block"
                                variant="body2"
                                onClick={(): void => {
                                    props.onLinkClick &&
                                        props.onLinkClick(props.values[0]);
                                }}
                            >
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    text={props.valueTextFn(props.values[0])}
                                />
                            </Link>
                        )}

                        {/* Render as text */}
                        {!props.link && (
                            <Typography variant="body2">
                                <TextHighlighter
                                    searchWords={props.searchWords}
                                    text={props.valueTextFn(props.values[0])}
                                />
                            </Typography>
                        )}
                    </>
                )}
            </Grid>
        </Grid>
    );
}
