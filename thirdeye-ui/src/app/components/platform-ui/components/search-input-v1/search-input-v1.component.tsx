// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { IconButton, InputAdornment, TextField } from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import SearchIcon from "@material-ui/icons/Search";
import classNames from "classnames";
import { debounce } from "lodash";
import React, {
    ChangeEvent,
    FunctionComponent,
    useCallback,
    useEffect,
    useRef,
    useState,
} from "react";
import { SearchInputV1Props } from "./search-input-v1.interfaces";

const DELAY_ON_CHANGE_DEFAULT = 200;

export const SearchInputV1: FunctionComponent<SearchInputV1Props> = ({
    value,
    placeholder,
    fullWidth,
    className,
    onChangeDelay = DELAY_ON_CHANGE_DEFAULT,
    onChange,
    ...otherProps
}) => {
    const [valueInternal, setValueInternal] = useState(value || "");
    const searchInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        // Input received
        setValueInternal(valueInternal);
    }, [value]);

    useEffect(() => {
        // Notify
        onChangeDebounced(valueInternal);
    }, [valueInternal]);

    const onChangeDebounced = useCallback(
        debounce((value: string): void => {
            onChange && onChange(value);
        }, onChangeDelay),
        [onChange]
    );

    const handleClearClick = (): void => {
        setValueInternal("");

        // Set focus
        searchInputRef &&
            searchInputRef.current &&
            searchInputRef.current.focus();
    };

    const handleInputChange = (event: ChangeEvent<HTMLInputElement>): void => {
        setValueInternal(event.target.value);
    };

    return (
        <TextField
            {...otherProps}
            InputProps={{
                startAdornment: (
                    // Search icon
                    <InputAdornment position="start">
                        <SearchIcon color="action" fontSize="small" />
                    </InputAdornment>
                ),
                endAdornment: (
                    // Clear button
                    <InputAdornment position="end">
                        {valueInternal && (
                            <IconButton
                                className="search-input-v1-clear-button"
                                size="small"
                                onClick={handleClearClick}
                            >
                                <CloseIcon color="action" fontSize="small" />
                            </IconButton>
                        )}
                    </InputAdornment>
                ),
            }}
            className={classNames(className, "search-input-v1")}
            fullWidth={fullWidth}
            inputRef={searchInputRef}
            placeholder={placeholder}
            value={valueInternal}
            onChange={handleInputChange}
        />
    );
};
