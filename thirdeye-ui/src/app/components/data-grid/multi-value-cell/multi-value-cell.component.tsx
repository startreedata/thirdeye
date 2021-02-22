import { Box, Grid, IconButton, Link } from "@material-ui/core";
import MoreHorizIcon from "@material-ui/icons/MoreHoriz";
import React, { Fragment, ReactElement } from "react";
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../../utils/material-ui/common.styles";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { MultiValueCellProps } from "./multi-value-cell.interfaces";

const MAX_ITEMS_VALUES = 3;

export function MultiValueCell<T>(props: MultiValueCellProps<T>): ReactElement {
    const commonClasses = useCommonStyles();
    const { t } = useTranslation();

    const getValue = (value: T): string => {
        if (props.valueTextFn) {
            return props.valueTextFn(value);
        }

        if (typeof value === "string") {
            return value;
        }

        return "";
    };

    const handleMore = (): void => {
        props.onMore && props.onMore(props.id);
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
                <Grid item className={commonClasses.ellipsis} xs={11}>
                    {props.values &&
                        props.values
                            .slice(0, MAX_ITEMS_VALUES)
                            .map((value, index) => (
                                <Fragment key={index}>
                                    {/* Value as text */}
                                    {!props.link && (
                                        <TextHighlighter
                                            searchWords={props.searchWords}
                                            text={getValue(value)}
                                        />
                                    )}

                                    {/* Value as link */}
                                    {props.link && (
                                        <Link
                                            onClick={() =>
                                                props.onClick &&
                                                props.onClick(value)
                                            }
                                        >
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                text={getValue(value) as string}
                                            />
                                        </Link>
                                    )}

                                    {/* Separator */}
                                    {index !==
                                        props.values.slice(0, MAX_ITEMS_VALUES)
                                            .length -
                                            1 && (
                                        <>{t("label.comma-separator")}</>
                                    )}
                                </Fragment>
                            ))}
                </Grid>

                {/* More button */}
                <Grid item xs={1}>
                    <IconButton size="small" onClick={handleMore}>
                        <MoreHorizIcon />
                    </IconButton>
                </Grid>
            </Grid>
        </Box>
    );
}
