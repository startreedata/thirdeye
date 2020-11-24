import { InputAdornment, TextField } from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import React, { ChangeEvent, FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { SearchProps } from "./search.interfaces";

export const Search: FunctionComponent<SearchProps> = (props: SearchProps) => {
    const { t } = useTranslation();

    const onChange = (event: ChangeEvent<HTMLInputElement>): void => {
        if (!event || !event.currentTarget) {
            return;
        }

        // Split input text into words
        const text = event.currentTarget.value;
        const searchWords = text ? text.trim().split(" ") : [];

        props.onChange && props.onChange(searchWords);
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
            label={t("label.search")}
            variant="outlined"
            onChange={onChange}
        />
    );
};
