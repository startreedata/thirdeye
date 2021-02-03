import {
    Card,
    CardContent,
    Grid,
    IconButton,
    List,
    ListItem,
    ListItemSecondaryAction,
    ListItemText,
    Typography,
} from "@material-ui/core";
import { ArrowForward, Close } from "@material-ui/icons";
import { isEmpty } from "lodash";
import React, { ReactElement, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { NoDataIndicator } from "../no-data-indicator/no-data-indicator.component";
import { SearchBar } from "../search-bar/search-bar.component";
import { TextHighlighter } from "../text-highlighter/text-highlighter.component";
import { TransferListProps } from "./transfer-list.interfaces";
import { useTransferListStyles } from "./transfer-list.styles";

export function TransferList<T>(props: TransferListProps<T>): ReactElement {
    const transferListClasses = useTransferListStyles();
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
        // Input changed, populate to-list maps
        populateListMaps();
    }, [props.fromList, props.toList]);

    useEffect(() => {
        // List map or search words changed, populate filtered to-list
        setFilteredToList(populateFilteredList(toListMap, toSearchWords));
    }, [toListMap, toSearchWords]);

    useEffect(() => {
        // List map or search words changed, populate filtered from-list
        setFilteredFromList(populateFilteredList(fromListMap, fromSearchWords));
    }, [fromListMap, fromSearchWords]);

    const populateListMaps = (): void => {
        const newToListMap = new Map<string | number, T>();
        const newFromListMap = new Map<string | number, T>();

        if (props.toList) {
            for (const toListItem of props.toList) {
                const key = props.listItemKeyFn(toListItem);
                if (!key) {
                    continue;
                }

                newToListMap.set(key, toListItem);
            }
        }

        if (props.fromList) {
            for (const fromListItem of props.fromList) {
                const key = props.listItemKeyFn(fromListItem);
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
        const newFilteredList: T[] = [];

        if (isEmpty(listMap)) {
            return newFilteredList;
        }

        for (const item of listMap.values()) {
            if (isEmpty(searchWords)) {
                newFilteredList.push(item);

                continue;
            }

            // Try to see if item matches any of the search words
            const itemText =
                props.listItemTextFn(item) ||
                (props.listItemKeyFn(item) as string);
            for (const searchWord of searchWords) {
                if (
                    itemText
                        .toLocaleLowerCase()
                        .indexOf(searchWord.toLowerCase()) > -1
                ) {
                    newFilteredList.push(item);

                    break;
                }
            }
        }

        // Reverse the list so that latest addition to the map will be at the top of the list
        return newFilteredList.reverse();
    };

    const onTransferListItem = (item: T): void => {
        const key = props.listItemKeyFn(item);
        if (!key) {
            return;
        }

        // Clone from-list map and delete item
        const newFromListMap = new Map(fromListMap);
        newFromListMap.delete(key);

        // Clone to-list map and add item
        const newToListMap = new Map(toListMap);
        newToListMap.set(key, item);

        // Update
        setFromListMap(newFromListMap);
        setToListMap(newToListMap);

        // Notify
        props.onChange && props.onChange([...newToListMap.values()]);
    };

    const onRemoveListItem = (item: T): void => {
        const key = props.listItemKeyFn(item);
        if (!key) {
            return;
        }

        // Clone to-list map and delete item
        const newToListMap = new Map(toListMap);
        newToListMap.delete(key);

        // Clone from-list map and add item
        const newFromListMap = new Map(fromListMap);
        newFromListMap.set(key, item);

        // Update
        setToListMap(newToListMap);
        setFromListMap(newFromListMap);

        // Notify
        props.onChange && props.onChange([...newToListMap.values()]);
    };

    return (
        <Grid container>
            {/* From-list*/}
            <Grid item sm={6}>
                <Grid container>
                    {/* Label */}
                    <Grid item sm={12}>
                        <Typography variant="subtitle1">
                            {props.fromLabel}
                        </Typography>
                    </Grid>

                    {/* Search */}
                    <Grid item sm={12}>
                        <SearchBar
                            searchLabel={t("label.search")}
                            searchStatusLabel={t("label.search-count", {
                                count: filteredFromList
                                    ? filteredFromList.length
                                    : 0,
                                total: fromListMap ? fromListMap.size : 0,
                            })}
                            onChange={setFromSearchWords}
                        />
                    </Grid>

                    <Grid item sm={12}>
                        <Card variant="outlined">
                            <CardContent
                                className={transferListClasses.listContainer}
                            >
                                {/* List */}
                                {!isEmpty(filteredFromList) && (
                                    <List dense>
                                        {filteredFromList.map(
                                            (fromListItem, index) => (
                                                <ListItem
                                                    button
                                                    key={index}
                                                    onClick={(): void => {
                                                        onTransferListItem(
                                                            fromListItem
                                                        );
                                                    }}
                                                >
                                                    <ListItemText
                                                        primary={
                                                            <TextHighlighter
                                                                searchWords={
                                                                    fromSearchWords
                                                                }
                                                                text={
                                                                    (props.listItemTextFn(
                                                                        fromListItem
                                                                    ) ||
                                                                        props.listItemKeyFn(
                                                                            fromListItem
                                                                        )) as string
                                                                }
                                                            />
                                                        }
                                                        primaryTypographyProps={{
                                                            variant: "body1",
                                                            className:
                                                                transferListClasses.listItem,
                                                        }}
                                                    />

                                                    {/* Transfer button */}
                                                    <ListItemSecondaryAction>
                                                        <IconButton
                                                            onClick={(): void => {
                                                                onTransferListItem(
                                                                    fromListItem
                                                                );
                                                            }}
                                                        >
                                                            <ArrowForward />
                                                        </IconButton>
                                                    </ListItemSecondaryAction>
                                                </ListItem>
                                            )
                                        )}
                                    </List>
                                )}

                                {/* No search results available message */}
                                {isEmpty(filteredFromList) &&
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
            <Grid item sm={6}>
                <Grid container>
                    {/* Label */}
                    <Grid item sm={12}>
                        <Typography variant="subtitle1">
                            {props.toLabel}
                        </Typography>
                    </Grid>

                    {/* Search */}
                    <Grid item sm={12}>
                        <SearchBar
                            searchLabel={t("label.search")}
                            searchStatusLabel={t("label.search-count", {
                                count: filteredToList
                                    ? filteredToList.length
                                    : 0,
                                total: toListMap ? toListMap.size : 0,
                            })}
                            onChange={setToSearchWords}
                        />
                    </Grid>

                    <Grid item sm={12}>
                        <Card variant="outlined">
                            <CardContent
                                className={transferListClasses.listContainer}
                            >
                                {/* List */}
                                {!isEmpty(filteredToList) && (
                                    <List dense>
                                        {filteredToList &&
                                            filteredToList.map(
                                                (toItem, index) => (
                                                    <ListItem
                                                        button
                                                        key={index}
                                                        onClick={(): void => {
                                                            onRemoveListItem(
                                                                toItem
                                                            );
                                                        }}
                                                    >
                                                        <ListItemText
                                                            primary={
                                                                <TextHighlighter
                                                                    searchWords={
                                                                        toSearchWords
                                                                    }
                                                                    text={
                                                                        (props.listItemTextFn(
                                                                            toItem
                                                                        ) ||
                                                                            props.listItemKeyFn(
                                                                                toItem
                                                                            )) as string
                                                                    }
                                                                />
                                                            }
                                                            primaryTypographyProps={{
                                                                variant:
                                                                    "body1",
                                                                className:
                                                                    transferListClasses.listItem,
                                                            }}
                                                        />

                                                        {/* Remove button */}
                                                        <ListItemSecondaryAction>
                                                            <IconButton
                                                                onClick={(): void => {
                                                                    onRemoveListItem(
                                                                        toItem
                                                                    );
                                                                }}
                                                            >
                                                                <Close />
                                                            </IconButton>
                                                        </ListItemSecondaryAction>
                                                    </ListItem>
                                                )
                                            )}
                                    </List>
                                )}

                                {/* No search results available message */}
                                {isEmpty(filteredToList) &&
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
