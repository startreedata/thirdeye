import {
    IconButton,
    InputAdornment,
    TextField,
    Typography,
} from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import SearchIcon from "@material-ui/icons/Search";
import { debounce, isNil } from "lodash";
import React, {
    ChangeEvent,
    FunctionComponent,
    useCallback,
    useEffect,
    useRef,
    useState,
} from "react";
import {
    getSearchFromQueryString,
    getSearchTextFromQueryString,
    setSearchInQueryString,
    setSearchTextInQueryString,
} from "../../utils/params/params.util";
import { SearchBarProps } from "./search-bar.interfaces";

const DELIMITER_SEARCH_WORDS = " ";
const DELAY_HANDLE_ON_CHANGE = 400;

export const SearchBar: FunctionComponent<SearchBarProps> = (
    props: SearchBarProps
) => {
    const [searchText, setSearchText] = useState(props.searchText || "");
    const searchInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        // Pick up search from query string if search text not provided
        if (props.searchText || !props.setSearchQueryString) {
            return;
        }

        // If search label matches search query string, set search text from query string
        if (props.searchLabel === getSearchFromQueryString()) {
            // Update search text and arrange to send event with a delay to allow the user to notice
            // search
            updateSearchText(getSearchTextFromQueryString(), true);
        }
    }, []);

    useEffect(() => {
        if (isNil(props.searchText)) {
            return;
        }

        // Input changed, update search text and arrange to send event with a delay to allow the
        // user to notice search
        updateSearchText(props.searchText || "", true);
    }, [props.searchText]);

    const handleInputChange = (event: ChangeEvent<HTMLInputElement>): void => {
        // Update search text and arrange to send event with a delay to account for a burst of
        // change events
        updateSearchText(event.currentTarget.value, true);
    };

    const handleClearClick = (): void => {
        // Update search text and arrange to send event immediately
        updateSearchText("", false);
        // Set focus
        searchInputRef &&
            searchInputRef.current &&
            searchInputRef.current.focus();
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

        // Set search in query string
        if (props.setSearchQueryString) {
            setSearchInQueryString(props.searchLabel);
            setSearchTextInQueryString(
                searchWords.join(DELIMITER_SEARCH_WORDS)
            );
        }
    };

    const sendOnChangeDebounced = useCallback(
        debounce(sendOnChange, DELAY_HANDLE_ON_CHANGE),
        [props.onChange, props.setSearchQueryString, props.searchLabel]
    );

    return (
        <TextField
            fullWidth
            InputProps={{
                startAdornment: (
                    // Search icon
                    <InputAdornment position="start">
                        <SearchIcon fontSize="small" />
                    </InputAdornment>
                ),
                endAdornment: (
                    <>
                        {/* Search status label */}
                        <InputAdornment position="end">
                            <Typography variant="body2">
                                {props.searchStatusLabel}
                            </Typography>
                        </InputAdornment>

                        {/* Clear button */}
                        <InputAdornment position="end">
                            <IconButton onClick={handleClearClick}>
                                <CloseIcon fontSize="small" />
                            </IconButton>
                        </InputAdornment>
                    </>
                ),
            }}
            autoFocus={props.autoFocus}
            inputRef={searchInputRef}
            placeholder={props.searchLabel}
            value={searchText}
            variant="outlined"
            onChange={handleInputChange}
        />
    );
};
