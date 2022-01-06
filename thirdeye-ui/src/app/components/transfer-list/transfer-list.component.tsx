import {
    Card,
    CardContent,
    Grid,
    IconButton,
    Link,
    List,
    ListItem,
    ListItemSecondaryAction,
    ListItemText,
    Typography,
} from "@material-ui/core";
import ArrowForwardIcon from "@material-ui/icons/ArrowForward";
import CloseIcon from "@material-ui/icons/Close";
import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import classnames from "classnames";
import { produce } from "immer";
import { isEmpty } from "lodash";
import React, { ReactElement, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../utils/material-ui/common.styles";
import { getSearchStatusLabel } from "../../utils/search/search.util";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import { TransferListProps } from "./transfer-list.interfaces";
import { useTransferListStyles } from "./transfer-list.styles";

export function TransferList<T>(props: TransferListProps<T>): ReactElement {
    const transferListClasses = useTransferListStyles();
    const commonClasses = useCommonStyles();
    const [fromListMap, setFromListMap] = useState<Map<string | number, T>>(
        new Map()
    );
    const [filteredFromList, setFilteredFromList] = useState<T[]>([]);
    const [toListMap, setToListMap] = useState<Map<string | number, T>>(
        new Map()
    );
    const [filteredToList, setFilteredToList] = useState<T[]>([]);
    const [fromSearchWords, setFromSearchWords] = useState<string[]>([]);
    const [toSearchWords, setToSearchWords] = useState<string[]>([]);
    const { t } = useTranslation();

    useEffect(() => {
        // Input changed, populate list maps
        populateListMaps();
    }, [props.fromList, props.toList]);

    useEffect(() => {
        // List map or search changed, populate filtered to-list
        setFilteredToList(populateFilteredList(toListMap, toSearchWords));
    }, [toListMap, toSearchWords]);

    useEffect(() => {
        // List map or search changed, populate filtered from-list
        setFilteredFromList(populateFilteredList(fromListMap, fromSearchWords));
    }, [fromListMap, fromSearchWords]);

    const populateListMaps = (): void => {
        const newToListMap = new Map();
        const newFromListMap = new Map();

        if (props.toList) {
            for (const toListItem of props.toList) {
                const key = getListItemKey(toListItem);
                if (!key) {
                    continue;
                }

                newToListMap.set(key, toListItem);
            }
        }

        if (props.fromList) {
            for (const fromListItem of props.fromList) {
                const key = getListItemKey(fromListItem);
                if (!key) {
                    continue;
                }

                if (newToListMap.has(key)) {
                    // Item already added to to-list
                    continue;
                }

                newFromListMap.set(key, fromListItem);
            }
        }

        setToListMap(newToListMap);
        setFromListMap(newFromListMap);
    };

    const populateFilteredList = (
        listMap: Map<string | number, T>,
        searchWords: string[]
    ): T[] => {
        if (isEmpty(listMap)) {
            return [];
        }

        const newFilteredList = [];
        for (const listItem of listMap.values()) {
            if (isEmpty(searchWords)) {
                newFilteredList.push(listItem);

                continue;
            }

            // Try to see if item matches any of the search words
            const listItemText = getListItemText(listItem);
            for (const searchWord of searchWords) {
                if (
                    listItemText
                        .toLowerCase()
                        .indexOf(searchWord.toLowerCase()) > -1
                ) {
                    newFilteredList.push(listItem);

                    break;
                }
            }
        }

        // Reverse the list so that latest addition to the map will be at the top of the list
        return newFilteredList.reverse();
    };

    const getListItemKey = (listItem: T): string | number => {
        if (props.listItemKeyFn) {
            return props.listItemKeyFn(listItem);
        }

        if (typeof listItem === "string") {
            return listItem;
        }

        return "";
    };

    const getListItemText = (listItem: T): string => {
        if (props.listItemTextFn) {
            return props.listItemTextFn(listItem);
        }

        if (typeof listItem === "string") {
            return listItem;
        }

        return "";
    };

    const handleListItemTransfer = (listItem: T): void => {
        const key = getListItemKey(listItem);
        if (!key) {
            return;
        }

        // Update to-list map
        const newToListMap = new Map(toListMap);
        newToListMap.set(key, listItem);
        setToListMap(newToListMap);

        // Update from-list map
        setFromListMap((fromList) => {
            const newFromListMap = new Map(fromList);
            newFromListMap.delete(key);

            return newFromListMap;
        });

        props.onChange && props.onChange([...newToListMap.values()]);
    };

    const handleListItemRemove = (listItem: T): void => {
        const key = getListItemKey(listItem);
        if (!key) {
            return;
        }

        // Update to-list map
        const newToListMap = new Map(toListMap);
        newToListMap.delete(key);
        setToListMap(newToListMap);

        // Update from-list map
        setFromListMap(
            produce((draft) => {
                draft.set(key, listItem);
            })
        );

        props.onChange && props.onChange([...newToListMap.values()]);
    };

    return (
        <Grid container>
            {/* From-list */}
            <Grid item sm={6} xs={12}>
                <Grid container>
                    {/* Label */}
                    <Grid item xs={12}>
                        <Typography variant="subtitle1">
                            {props.fromLabel}
                        </Typography>
                    </Grid>

                    {/* Search */}
                    <Grid item xs={12}>
                        <SearchBar
                            searchLabel={t("label.search")}
                            searchStatusLabel={getSearchStatusLabel(
                                filteredFromList ? filteredFromList.length : 0,
                                fromListMap ? fromListMap.size : 0
                            )}
                            onChange={setFromSearchWords}
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <Card variant="outlined">
                            <CardContent
                                className={classnames(
                                    transferListClasses.list,
                                    commonClasses.cardContentBottomPaddingRemoved
                                )}
                            >
                                {/* List */}
                                {!props.loading && (
                                    <List disablePadding>
                                        {filteredFromList &&
                                            filteredFromList.map(
                                                (fromListItem, index) => (
                                                    <ListItem
                                                        divider
                                                        key={index}
                                                    >
                                                        <ListItemText
                                                            primary={
                                                                <>
                                                                    {/* Text */}
                                                                    {!props.link && (
                                                                        <TextHighlighter
                                                                            searchWords={
                                                                                fromSearchWords
                                                                            }
                                                                            text={getListItemText(
                                                                                fromListItem
                                                                            )}
                                                                        />
                                                                    )}

                                                                    {/* Link */}
                                                                    {props.link && (
                                                                        <Link
                                                                            component="button"
                                                                            variant="body2"
                                                                            onClick={() =>
                                                                                props.onClick &&
                                                                                props.onClick(
                                                                                    fromListItem
                                                                                )
                                                                            }
                                                                        >
                                                                            <TextHighlighter
                                                                                searchWords={
                                                                                    fromSearchWords
                                                                                }
                                                                                text={getListItemText(
                                                                                    fromListItem
                                                                                )}
                                                                            />
                                                                        </Link>
                                                                    )}
                                                                </>
                                                            }
                                                            primaryTypographyProps={{
                                                                variant:
                                                                    "body2",
                                                            }}
                                                        />

                                                        {/* Transfer button */}
                                                        <ListItemSecondaryAction>
                                                            <IconButton
                                                                onClick={() =>
                                                                    handleListItemTransfer(
                                                                        fromListItem
                                                                    )
                                                                }
                                                            >
                                                                <ArrowForwardIcon fontSize="small" />
                                                            </IconButton>
                                                        </ListItemSecondaryAction>
                                                    </ListItem>
                                                )
                                            )}
                                    </List>
                                )}

                                {/* Loading indicator */}
                                {props.loading && <AppLoadingIndicatorV1 />}

                                {/* No search results available message */}
                                {!props.loading &&
                                    isEmpty(filteredFromList) &&
                                    !isEmpty(fromSearchWords) && (
                                        <NoDataIndicator
                                            text={t(
                                                "message.no-search-results"
                                            )}
                                        />
                                    )}
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            </Grid>

            {/* To-list */}
            <Grid item sm={6} xs={12}>
                <Grid container>
                    {/* Label */}
                    <Grid item xs={12}>
                        <Typography variant="subtitle1">
                            {props.toLabel}
                        </Typography>
                    </Grid>

                    {/* Search */}
                    <Grid item xs={12}>
                        <SearchBar
                            searchLabel={t("label.search")}
                            searchStatusLabel={getSearchStatusLabel(
                                filteredToList ? filteredToList.length : 0,
                                toListMap ? toListMap.size : 0
                            )}
                            onChange={setToSearchWords}
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <Card variant="outlined">
                            <CardContent
                                className={classnames(
                                    transferListClasses.list,
                                    commonClasses.cardContentBottomPaddingRemoved
                                )}
                            >
                                {/* List */}
                                {!props.loading && (
                                    <List disablePadding>
                                        {filteredToList &&
                                            filteredToList.map(
                                                (toListItem, index) => (
                                                    <ListItem
                                                        divider
                                                        key={index}
                                                    >
                                                        <ListItemText
                                                            primary={
                                                                <>
                                                                    {/* Text */}
                                                                    {!props.link && (
                                                                        <TextHighlighter
                                                                            searchWords={
                                                                                toSearchWords
                                                                            }
                                                                            text={getListItemText(
                                                                                toListItem
                                                                            )}
                                                                        />
                                                                    )}

                                                                    {/* Link */}
                                                                    {props.link && (
                                                                        <Link
                                                                            component="button"
                                                                            variant="body2"
                                                                            onClick={() =>
                                                                                props.onClick &&
                                                                                props.onClick(
                                                                                    toListItem
                                                                                )
                                                                            }
                                                                        >
                                                                            <TextHighlighter
                                                                                searchWords={
                                                                                    toSearchWords
                                                                                }
                                                                                text={getListItemText(
                                                                                    toListItem
                                                                                )}
                                                                            />
                                                                        </Link>
                                                                    )}
                                                                </>
                                                            }
                                                            primaryTypographyProps={{
                                                                variant:
                                                                    "body2",
                                                            }}
                                                        />

                                                        {/* Remove button */}
                                                        <ListItemSecondaryAction>
                                                            <IconButton
                                                                onClick={() =>
                                                                    handleListItemRemove(
                                                                        toListItem
                                                                    )
                                                                }
                                                            >
                                                                <CloseIcon fontSize="small" />
                                                            </IconButton>
                                                        </ListItemSecondaryAction>
                                                    </ListItem>
                                                )
                                            )}
                                    </List>
                                )}

                                {/* Loading indicator */}
                                {props.loading && <AppLoadingIndicatorV1 />}

                                {/* No search results available message */}
                                {!props.loading &&
                                    isEmpty(filteredToList) &&
                                    !isEmpty(toSearchWords) && (
                                        <NoDataIndicator
                                            text={t(
                                                "message.no-search-results"
                                            )}
                                        />
                                    )}
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
}
