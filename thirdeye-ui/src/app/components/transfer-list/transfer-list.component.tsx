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
    const [transferActivity, setTransferActivity] = useState(true);
    const { t } = useTranslation();

    useEffect(() => {
        // Input changed, populate to-list maps
        populateToListMap();
    }, [props.toList]);

    useEffect(() => {
        // Input, or to-list map changed, populate from-list maps
        populateFromListMap();
    }, [props.fromList, toListMap]);

    useEffect(() => {
        // List map or search words changed, populate filtered to-list
        setFilteredToList(populateFilteredList(toListMap, toSearchWords));
    }, [toListMap, toSearchWords, transferActivity]);

    useEffect(() => {
        // List map or search words changed, populate filtered from-list
        setFilteredFromList(populateFilteredList(fromListMap, fromSearchWords));
    }, [fromListMap, fromSearchWords, transferActivity]);

    const populateToListMap = (): void => {
        const newToListMap = new Map<string | number, T>();
        for (const toItem of props.toList) {
            const key = props.getKey(toItem);
            if (!key) {
                // Ignore
                continue;
            }

            newToListMap.set(key, toItem);
        }

        setToListMap(newToListMap);
    };

    const populateFromListMap = (): void => {
        const newFromListMap = new Map<string | number, T>();
        for (const fromItem of props.fromList) {
            const key = props.getKey(fromItem);
            if (!key) {
                // Ignore
                continue;
            }

            if (toListMap.has(key)) {
                // Item already selected
                continue;
            }

            newFromListMap.set(key, fromItem);
        }

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
            const itemRenderString = props.renderer(item) || "";
            for (const toSearchWord of searchWords) {
                if (
                    itemRenderString
                        .toLocaleLowerCase()
                        .indexOf(toSearchWord.toLowerCase()) > -1
                ) {
                    newFilteredList.push(item);

                    break;
                }
            }
        }

        return newFilteredList;
    };

    const onTransfer = (item: T): void => {
        const key = props.getKey(item);
        if (!key) {
            return;
        }

        // Remove item from from-list map
        fromListMap.delete(key);

        // Add item to to-list map
        toListMap.set(key, item);

        // Activate list and map changes
        setTransferActivity((transferActivity) => !transferActivity);
        props.onChange && props.onChange([...toListMap.values()]);
    };

    const onRemove = (item: T): void => {
        const key = props.getKey(item);
        if (!key) {
            return;
        }

        // Remove item from to-list map
        toListMap.delete(key);

        // Add item to from-list map
        fromListMap.set(key, item);

        // Activate list and map changes
        setTransferActivity((transferActivity) => !transferActivity);
        props.onChange && props.onChange([...toListMap.values()]);
    };

    return (
        <Grid container>
            {/* Title */}
            {props.title && (
                <Grid item md={12}>
                    <Typography variant="h6">{props.title}</Typography>
                </Grid>
            )}

            {/* From */}
            <Grid item md={6}>
                <Grid container>
                    {/* Label */}
                    <Grid item md={12}>
                        <Typography variant="body2">
                            <strong>{props.fromLabel}</strong>
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
                                            (fromItem, index) => (
                                                <ListItem
                                                    button
                                                    key={index}
                                                    onClick={(): void => {
                                                        onTransfer(fromItem);
                                                    }}
                                                >
                                                    <ListItemText
                                                        primary={props.renderer(
                                                            fromItem
                                                        )}
                                                        primaryTypographyProps={{
                                                            variant: "body1",
                                                        }}
                                                    />

                                                    {/* Transfer icon */}
                                                    <ListItemSecondaryAction>
                                                        <IconButton
                                                            onClick={(): void => {
                                                                onTransfer(
                                                                    fromItem
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

            {/* To */}
            <Grid item md={6}>
                <Grid container>
                    {/* Label */}
                    <Grid item md={12}>
                        <Typography variant="body2">
                            <strong>{props.toLabel}</strong>
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
                                                    onRemove(toItem);
                                                }}
                                            >
                                                <ListItemText
                                                    primary={props.renderer(
                                                        toItem
                                                    )}
                                                    primaryTypographyProps={{
                                                        variant: "body1",
                                                    }}
                                                />

                                                {/* Remove icon */}
                                                <ListItemSecondaryAction>
                                                    <IconButton
                                                        onClick={(): void => {
                                                            onRemove(toItem);
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
