import { fade, InputBase, makeStyles } from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import React, { ReactElement } from "react";

type Props = {
    onSearch: (search: string) => void;
    searchValue: string;
    placeholder?: string;
};

const useStyles = makeStyles((theme) => {
    return {
        search: {
            position: "relative",
            border: "1px solid rgba(25, 25, 25, 0.32)",
            boxSizing: "border-box",
            borderRadius: 8,
            backgroundColor: fade(theme.palette.common.white, 0.15),
            "&:hover": {
                backgroundColor: fade(theme.palette.common.white, 0.25),
            },
            marginLeft: 0,
            minWidth: 320,
            [theme.breakpoints.up("sm")]: {
                width: "auto",
            },
        },
        searchIcon: {
            padding: theme.spacing(0, 2),
            height: "100%",
            position: "absolute",
            pointerEvents: "none",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            top: 0,
            right: 0,
        },
        inputRoot: {
            color: "inherit",
            display: "block",
        },
        inputInput: {
            width: "calc(100% - 56px)",
            padding: theme.spacing(2.5, 1),
            paddingRight: `56px`,
            transition: theme.transitions.create("width"),
        },
    };
});

const SearchBar = ({
    searchValue,
    onSearch,
    placeholder = "Search by Alert Name",
}: Props): ReactElement => {
    const classes = useStyles();

    return (
        <div className={classes.search}>
            <InputBase
                classes={{
                    root: classes.inputRoot,
                    input: classes.inputInput,
                }}
                inputProps={{ "aria-label": "search" }}
                placeholder={placeholder}
                value={searchValue || ""}
                onChange={(e): void => {
                    onSearch(e.target.value);
                }}
            />
            <div className={classes.searchIcon}>
                <SearchIcon htmlColor="#DADADA" />
            </div>
        </div>
    );
};

export default SearchBar;
