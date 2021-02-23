import { Box, Grid, IconButton, Link, Typography } from "@material-ui/core";
import MoreHorizIcon from "@material-ui/icons/MoreHoriz";
import classnames from "classnames";
import React, { Fragment, ReactElement } from "react";
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../../utils/material-ui/common.styles";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { MultiValueCellProps } from "./multi-value-cell.interfaces";
import { useMultiValueCellStyles } from "./multi-value-cell.styles";

const MAX_ITEMS_VALUES = 3;

export function MultiValueCell<T>(props: MultiValueCellProps<T>): ReactElement {
    const multiValueCellClasses = useMultiValueCellStyles();
    const commonClasses = useCommonStyles();
    const { t } = useTranslation();

    const getValueText = (value: T): string => {
        if (props.valueTextFn) {
            return props.valueTextFn(value);
        }

        if (typeof value === "string") {
            return value;
        }

        return "";
    };

    const handleMore = (): void => {
        props.onMore && props.onMore(props.rowId);
    };

    return (
        <Box width="100%">
            <Grid
                container
                alignItems="center"
                justify="space-between"
                spacing={0}
            >
                {/* Values */}
                <Grid
                    item
                    className={classnames(commonClasses.ellipsis, {
                        [multiValueCellClasses.values]: props.link, // Ellipsis to have same color as link
                    })}
                    xs={11}
                >
                    {props.values &&
                        props.values
                            .slice(0, MAX_ITEMS_VALUES)
                            .map((value, index) => (
                                <Fragment key={index}>
                                    {/* Value as text */}
                                    {!props.link && (
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={getValueText(value)}
                                        />
                                    )}

                                    {/* Value as link */}
                                    {props.link && (
                                        <Link
                                            onClick={() =>
                                                props.onClick &&
                                                props.onClick(
                                                    value,
                                                    props.rowId
                                                )
                                            }
                                        >
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                text={getValueText(value)}
                                            />
                                        </Link>
                                    )}

                                    {/* Separator */}
                                    {index !==
                                        props.values.slice(0, MAX_ITEMS_VALUES)
                                            .length -
                                            1 && (
                                        <Typography
                                            color="textPrimary"
                                            display="inline"
                                        >
                                            {t("label.comma-separator")}
                                        </Typography>
                                    )}
                                </Fragment>
                            ))}
                </Grid>

                {/* More button */}
                {props.values && props.values.length > MAX_ITEMS_VALUES && (
                    <Grid item xs={1}>
                        <IconButton size="small" onClick={handleMore}>
                            <MoreHorizIcon fontSize="small" />
                        </IconButton>
                    </Grid>
                )}
            </Grid>
        </Box>
    );
}
