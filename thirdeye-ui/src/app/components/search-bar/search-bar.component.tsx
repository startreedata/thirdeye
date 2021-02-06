import { IconButton, InputAdornment, TextField } from "@material-ui/core";
import { Close, Search } from "@material-ui/icons";
import { debounce } from "lodash";
import React, {
    ChangeEvent,
    createRef,
    FunctionComponent,
    useCallback,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    getSearchFromQueryString,
    getSearchTextFromQueryString,
    setSearchInQueryString,
    setSearchTextInQueryString,
} from "../../utils/params/params.util";
import { SearchBarProps } from "./search-bar.interfaces";

const DELIMITER_SEARCH_WORDS = " ";

export const SearchBar: FunctionComponent<SearchBarProps> = (
    props: SearchBarProps
) => {
    const [searchText, setSearchText] = useState("");
    const inputRef = createRef<HTMLInputElement>();
    const { t } = useTranslation();

    useEffect(() => {
        // Pick up search from query string if required
        if (!props.setSearchQueryString) {
            return;
        }

        // If search label matches search query string, set search text from query string
        if (props.searchLabel === getSearchFromQueryString()) {
            // Update search text and arrange to send event with a delay to allow user to notice
            // search
            updateSearchText(getSearchTextFromQueryString(), true);
        }
    }, []);

    const onSearchInputChange = (
        event: ChangeEvent<HTMLInputElement>
    ): void => {
        // Update search text and arrange to send event with a delay to account for a burst of
        // change events
        updateSearchText(event.currentTarget.value, true);
    };

    const onClearSearch = (): void => {
        // Update search text and arrange to send event immediately
        updateSearchText("", false);

        // Set focus
        inputRef && inputRef.current && inputRef.current.focus();
    };

    const updateSearchText = (searchText: string, debounced: boolean): void => {
        setSearchText(searchText);

        // Split search text into words
        const searchWords = searchText
            ? searchText.trim().split(DELIMITER_SEARCH_WORDS)
            : [];

        // Depending on the flag, arrange to send change event immediately or with a delay
        debounced
            ? sendOnChangeDebounced(searchWords)
            : sendOnChange(searchWords);
    };

    const sendOnChange = (searchWords: string[]): void => {
        props.onChange && props.onChange(searchWords);

        // Set search and search text in query string
        if (props.setSearchQueryString) {
            setSearchInQueryString(
                props.searchLabel ? props.searchLabel : t("label.search")
            );
            setSearchTextInQueryString(
                searchWords.join(DELIMITER_SEARCH_WORDS)
            );
        }
    };

    const sendOnChangeDebounced = useCallback(debounce(sendOnChange, 400), [
        props.onChange,
        props.setSearchQueryString,
        props.searchLabel,
    ]);

    return (
        // Search bar
        <TextField
            fullWidth
            InputProps={{
                startAdornment: (
                    // Search icon
                    <InputAdornment position="start">
                        <Search />
                    </InputAdornment>
                ),
                endAdornment: (
                    <>
                        {/* Search status label */}
                        <InputAdornment position="end">
                            {props.searchStatusLabel}
                        </InputAdornment>

                        {/* Clear search button */}
                        <InputAdornment position="end">
                            <IconButton onClick={onClearSearch}>
                                <Close />
                            </IconButton>
                        </InputAdornment>
                    </>
                ),
            }}
            autoFocus={props.autoFocus}
            inputRef={inputRef}
            label={props.searchLabel ? props.searchLabel : t("label.search")}
            value={searchText}
            variant="outlined"
            onChange={onSearchInputChange}
        />
    );
};
