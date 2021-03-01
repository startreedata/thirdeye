import { Grid, IconButton, Link, Typography } from "@material-ui/core";
import { CellParams } from "@material-ui/data-grid";
import MoreHorizIcon from "@material-ui/icons/MoreHoriz";
import classnames from "classnames";
import { toNumber } from "lodash";
import React, { Fragment, ReactElement, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../../utils/material-ui/common.styles";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { MultiValueCellProps } from "./multi-value-cell.interfaces";
import { useMultiValueCellStyles } from "./multi-value-cell.styles";

const MAX_ITEMS_VALUES = 3;

function MultiValueCell<T>(props: MultiValueCellProps<T>): ReactElement {
    const multiValueCellClasses = useMultiValueCellStyles();
    const commonClasses = useCommonStyles();
    const [values, setValues] = useState<T[]>([]);
    const [rowId, setRowId] = useState(-1);
    const { t } = useTranslation();

    useEffect(() => {
        setValues(props.params && ((props.params.value as unknown) as T[]));
        setRowId(
            toNumber(props.params && props.params.row && props.params.row.id)
        );
    }, []);

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
        if (rowId < 0) {
            return;
        }

        props.onMore && props.onMore(rowId);
    };

    return (
        <div className={multiValueCellClasses.multiValueCell}>
            <Grid container spacing={0}>
                {/* Values */}
                <Grid
                    item
                    className={classnames(commonClasses.ellipsis, {
                        [multiValueCellClasses.linkValues]: props.link, // Ellipsis to have the same color as link
                    })}
                    xs={11}
                >
                    {values &&
                        values
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
                                        // Not to set component as button to enable keyboard focus,
                                        // links may be hidden
                                        <Link
                                            onClick={() =>
                                                props.onClick &&
                                                props.onClick(value, rowId)
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
                                        values.slice(0, MAX_ITEMS_VALUES)
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
                {values && values.length > MAX_ITEMS_VALUES && (
                    <Grid item xs={1}>
                        <IconButton size="small" onClick={handleMore}>
                            <MoreHorizIcon fontSize="small" />
                        </IconButton>
                    </Grid>
                )}
            </Grid>
        </div>
    );
}

export function multiValueCellRenderer<T>(
    params: CellParams,
    link?: boolean,
    searchWords?: string[],
    onMore?: (rowId: number) => void,
    onClick?: (value: T, rowId: number) => void,
    valueTextFn?: (value: T) => string
): ReactElement {
    return (
        <MultiValueCell<T>
            link={link}
            params={params}
            searchWords={searchWords}
            valueTextFn={valueTextFn}
            onClick={onClick}
            onMore={onMore}
        />
    );
}
