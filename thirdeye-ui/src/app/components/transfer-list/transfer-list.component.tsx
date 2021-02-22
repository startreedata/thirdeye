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
import ArrowForwardIcon from "@material-ui/icons/ArrowForward";
import CloseIcon from "@material-ui/icons/Close";
import { produce } from "immer";
import { isEmpty } from "lodash";
import React, { ReactElement, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { getSearchStatusLabel } from "../../utils/search/search.util";
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
            const listItemText = props.listItemTextFn(listItem) || "";
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

    const handleTransfer = (item: T): void => {
        const key = props.listItemKeyFn(item);
        if (!key) {
            return;
        }

        // Update to-list map
        const newToListMap = new Map(toListMap);
        newToListMap.set(key, item);
        setToListMap(newToListMap);

        // Update from-list map
        setFromListMap(
            produce((draft) => {
                draft.delete(key);
            })
        );

        // Notify
        props.onChange && props.onChange([...newToListMap.values()]);
    };

    const handleRemove = (item: T): void => {
        const key = props.listItemKeyFn(item);
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
                draft.set(key, item);
            })
        );

        // Notify
        props.onChange && props.onChange([...newToListMap.values()]);
    };

    return (
        <Grid container>
            {/* From-list*/}
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

                    {/* List */}
                    <Grid item xs={12}>
                        <Card variant="outlined">
                            <CardContent className={transferListClasses.list}>
                                <List dense>
                                    {filteredFromList &&
                                        filteredFromList.map(
                                            (fromListItem, index) => (
                                                <ListItem
                                                    button
                                                    key={index}
                                                    onClick={() =>
                                                        handleTransfer(
                                                            fromListItem
                                                        )
                                                    }
                                                >
                                                    <ListItemText
                                                        primary={
                                                            <TextHighlighter
                                                                searchWords={
                                                                    fromSearchWords
                                                                }
                                                                text={props.listItemTextFn(
                                                                    fromListItem
                                                                )}
                                                            />
                                                        }
                                                        primaryTypographyProps={{
                                                            variant: "body1",
                                                            noWrap: true,
                                                        }}
                                                    />

                                                    {/* Transfer button */}
                                                    <ListItemSecondaryAction>
                                                        <IconButton
                                                            onClick={() =>
                                                                handleTransfer(
                                                                    fromListItem
                                                                )
                                                            }
                                                        >
                                                            <ArrowForwardIcon />
                                                        </IconButton>
                                                    </ListItemSecondaryAction>
                                                </ListItem>
                                            )
                                        )}
                                </List>

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

                    {/* List */}
                    <Grid item xs={12}>
                        <Card variant="outlined">
                            <CardContent className={transferListClasses.list}>
                                <List dense>
                                    {filteredToList &&
                                        filteredToList.map((toItem, index) => (
                                            <ListItem
                                                button
                                                key={index}
                                                onClick={() =>
                                                    handleRemove(toItem)
                                                }
                                            >
                                                <ListItemText
                                                    primary={
                                                        <TextHighlighter
                                                            searchWords={
                                                                toSearchWords
                                                            }
                                                            text={props.listItemTextFn(
                                                                toItem
                                                            )}
                                                        />
                                                    }
                                                    primaryTypographyProps={{
                                                        variant: "body1",
                                                        noWrap: true,
                                                    }}
                                                />

                                                {/* Remove button */}
                                                <ListItemSecondaryAction>
                                                    <IconButton
                                                        onClick={() =>
                                                            handleRemove(toItem)
                                                        }
                                                    >
                                                        <CloseIcon />
                                                    </IconButton>
                                                </ListItemSecondaryAction>
                                            </ListItem>
                                        ))}
                                </List>

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
