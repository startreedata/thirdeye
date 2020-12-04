import { InputAdornment, TextField } from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import { debounce } from "lodash";
import React, { createRef, FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { SearchProps } from "./search.interfaces";

const SEARCH_KEY = "search";

export const Search: FunctionComponent<SearchProps> = (props: SearchProps) => {
    const { t } = useTranslation();
    const history = useHistory();
    const searchRef = createRef<HTMLInputElement>();

    const onChange = (): void => {
        // Split input text into words
        const text = searchRef.current?.value || "";

        const searchWords = text ? text.trim().split(" ") : [];

        sendOnChange(searchWords);
    };

    const sendOnChange = debounce((searchWords: string[]): void => {
        if (props.syncSearchWithURL) {
            // Update URL
            history.replace({
                search: new URLSearchParams(
                    searchWords.length
                        ? `${SEARCH_KEY}=${searchWords.join(" ")}`
                        : ""
                ).toString(),
            });
        }

        props.onChange && props.onChange(searchWords);
    }, 500);

    useEffect(() => {
        registerCustomEvents();
        updateSearchTextFromURL();
    }, []);

    const updateSearchTextFromURL = (): void => {
        if (!searchRef.current) {
            return;
        }

        const searchParam = new URLSearchParams(history.location.search);

        // Update TextField value if URLSeach present
        searchRef.current.value = searchParam.get(SEARCH_KEY) || "";

        // To fire changeEvent on search input
        searchRef.current.dispatchEvent(new CustomEvent("update"));
    };

    // Custom event attachemnt to update search
    const registerCustomEvents = (): (() => void) | undefined => {
        if (!searchRef.current) {
            return;
        }
        searchRef.current?.addEventListener("update", onChange);

        return (): void =>
            searchRef.current?.removeEventListener("update", onChange);
    };

    return (
        <TextField
            fullWidth
            InputProps={{
                startAdornment: (
                    <InputAdornment position="start">
                        <SearchIcon />
                    </InputAdornment>
                ),
                endAdornment: (
                    <InputAdornment position="end">
                        {props.searchStatusText}
                    </InputAdornment>
                ),
            }}
            autoFocus={props.autoFocus}
            inputRef={searchRef}
            label={t("label.search")}
            variant="outlined"
            onChange={onChange}
        />
    );
};
