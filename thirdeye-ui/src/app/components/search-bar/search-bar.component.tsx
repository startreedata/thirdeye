import { InputAdornment, TextField } from "@material-ui/core";
import { Search } from "@material-ui/icons";
import { debounce } from "lodash";
import React, {
    ChangeEvent,
    FunctionComponent,
    useCallback,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    getSearchFromQueryString,
    setSearchInQueryString,
} from "../../utils/params-util/params-util";
import { SearchBarProps } from "./search-bar.interfaces";

const SEARCH_WORD_DELIMITER = " ";

export const SearchBar: FunctionComponent<SearchBarProps> = (
    props: SearchBarProps
) => {
    const [searchText, setSearchText] = useState("");
    const { t } = useTranslation();

    useEffect(() => {
        // Set search text from search query string if available
        if (props.setSearchQueryString) {
            updateSearchText(getSearchFromQueryString());
        }
    }, []);

    const onChange = (event: ChangeEvent<HTMLInputElement>): void => {
        updateSearchText(event.currentTarget.value);
    };

    const updateSearchText = (searchText: string): void => {
        setSearchText(searchText);

        // Split search text into words
        const searchWords = searchText
            ? searchText.trim().split(SEARCH_WORD_DELIMITER)
            : [];

        sendOnChange(searchWords);
    };

    const sendOnChange = useCallback(
        debounce((searchWords: string[]): void => {
            props.onChange && props.onChange(searchWords);

            // Set search in query string
            if (props.setSearchQueryString) {
                setSearchInQueryString(searchWords.join(SEARCH_WORD_DELIMITER));
            }
        }, 500),
        []
    );

    return (
        <TextField
            fullWidth
            InputProps={{
                startAdornment: (
                    <InputAdornment position="start">
                        <Search />
                    </InputAdornment>
                ),
                endAdornment: (
                    <InputAdornment position="end">
                        {props.searchStatusText}
                    </InputAdornment>
                ),
            }}
            autoFocus={props.autoFocus}
            label={props.label ? props.label : t("label.search")}
            value={searchText}
            variant="outlined"
            onChange={onChange}
        />
    );
};
