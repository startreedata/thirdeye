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
import { SearchBar } from "../search-bar/search-bar.component";
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
                    // Ignore
                    continue;
                }

                newToListMap.set(key, toListItem);
            }
        }

        if (props.fromList) {
            for (const fromListItem of props.fromList) {
                const key = props.listItemKeyFn(fromListItem);
                if (!key) {
                    // Ignore
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
                // No search words
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

        return newFilteredList;
    };

    const onTransferListItem = (item: T): void => {
        const key = props.listItemKeyFn(item);
        if (!key) {
            return;
        }

        // Remove item from from-list map
        fromListMap.delete(key);

        // Add item to to-list map
        toListMap.set(key, item);

        props.onChange && props.onChange([...toListMap.values()]);
    };

    const onRemoveListItem = (item: T): void => {
        const key = props.listItemKeyFn(item);
        if (!key) {
            return;
        }

        // Remove item from to-list map
        toListMap.delete(key);

        // Add item to from-list map
        fromListMap.set(key, item);

        props.onChange && props.onChange([...toListMap.values()]);
    };

    return (
        <Grid container>
            {/* From-list*/}
            <Grid item md={6}>
                <Grid container>
                    {/* Label */}
                    <Grid item md={12}>
                        <Typography variant="subtitle2">
                            {props.fromLabel}
                        </Typography>
                    </Grid>

                    {/* Search */}
                    <Grid item md={12}>
                        <SearchBar
                            label={t("label.search")}
                            searchStatusLabel={t("label.search-count", {
                                count: filteredFromList
                                    ? filteredFromList.length
                                    : 0,
                                total: fromListMap ? fromListMap.size : 0,
                            })}
                            onChange={setFromSearchWords}
                        />
                    </Grid>

                    {/* List */}
                    <Grid item md={12}>
                        <Card variant="outlined">
                            <CardContent
                                className={transferListClasses.listContainer}
                            >
                                <List dense>
                                    {filteredFromList &&
                                        filteredFromList.map(
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
                                                            props.listItemTextFn(
                                                                fromListItem
                                                            ) ||
                                                            props.listItemKeyFn(
                                                                fromListItem
                                                            )
                                                        }
                                                        primaryTypographyProps={{
                                                            variant: "body1",
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
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            </Grid>

            {/* To-list */}
            <Grid item md={6}>
                <Grid container>
                    {/* Label */}
                    <Grid item md={12}>
                        <Typography variant="subtitle2">
                            {props.toLabel}
                        </Typography>
                    </Grid>

                    {/* Search */}
                    <Grid item md={12}>
                        <SearchBar
                            label={t("label.search")}
                            searchStatusLabel={t("label.search-count", {
                                count: filteredToList
                                    ? filteredToList.length
                                    : 0,
                                total: toListMap ? toListMap.size : 0,
                            })}
                            onChange={setToSearchWords}
                        />
                    </Grid>

                    {/* List */}
                    <Grid item md={12}>
                        <Card variant="outlined">
                            <CardContent
                                className={transferListClasses.listContainer}
                            >
                                <List dense>
                                    {filteredToList &&
                                        filteredToList.map((toItem, index) => (
                                            <ListItem
                                                button
                                                key={index}
                                                onClick={(): void => {
                                                    onRemoveListItem(toItem);
                                                }}
                                            >
                                                <ListItemText
                                                    primary={
                                                        props.listItemTextFn(
                                                            toItem
                                                        ) ||
                                                        props.listItemKeyFn(
                                                            toItem
                                                        )
                                                    }
                                                    primaryTypographyProps={{
                                                        variant: "body1",
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
                                        ))}
                                </List>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
}
