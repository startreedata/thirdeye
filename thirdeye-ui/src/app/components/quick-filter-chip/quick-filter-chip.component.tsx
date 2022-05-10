import {
    Chip,
    ClickAwayListener,
    InputAdornment,
    InputBase,
    Popper,
} from "@material-ui/core";
import ArrowDropDownIcon from "@material-ui/icons/ArrowDropDown";
import ClearIcon from "@material-ui/icons/Clear";
import SearchIcon from "@material-ui/icons/Search";
import { Autocomplete } from "@material-ui/lab";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
import { ChipFilterProps, FilterOption } from "./quick-filter-chip.interfaces";
import { useAutoCompleteStyles } from "./quick-filter-chip.styles";

export const ChipFilter: FunctionComponent<ChipFilterProps> = (
    props: ChipFilterProps
) => {
    const [anchorEl, setAnchorEl] = React.useState<HTMLDivElement | null>(null);

    const open = Boolean(anchorEl);
    const optionSelected = props.options.find((o) => o.id === props.value.id);
    const classes = useAutoCompleteStyles({ open });

    const handleClick = (event: React.MouseEvent<HTMLDivElement>): void => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = (): void => {
        setAnchorEl(null);
    };

    const clearFilter = (): void => {
        props.onFilter && props.onFilter();
    };

    const filterSelect = (filter?: FilterOption): void => {
        props.onFilter && props.onFilter(filter);
        handleClose();
    };

    return (
        <>
            <Chip
                classes={{
                    icon: classes.chipIcon,
                    deleteIcon: classes.sizeSmall,
                }}
                deleteIcon={optionSelected && <ClearIcon />}
                icon={<ArrowDropDownIcon />}
                label={
                    <>
                        {props.label}
                        {optionSelected ? ": " : " "}{" "}
                        <b>{optionSelected?.label || props.value.label}</b>
                    </>
                }
                style={{
                    maxWidth: "300px",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                    overflow: "hidden",
                }}
                title={`${props.label}${optionSelected ? ":" : " "} ${
                    optionSelected?.label || props.value.label
                }`}
                variant="outlined"
                onClick={handleClick}
                onDelete={optionSelected && clearFilter}
            />

            <Popper
                anchorEl={anchorEl}
                // container={Paper}
                className={classes.popper}
                id={props.label + "-popper"}
                open={open}
                placement="bottom-end"
            >
                <ClickAwayListener onClickAway={handleClose}>
                    <Autocomplete
                        disableCloseOnSelect
                        disablePortal
                        fullWidth
                        open
                        openOnFocus
                        classes={{
                            paper: classes.paper,
                            popperDisablePortal: classes.popperDisablePortal,
                        }}
                        getOptionLabel={(option) => option?.label}
                        noOptionsText={`No ${props.label}`}
                        options={props.options || []}
                        renderInput={(params) => (
                            <InputBase
                                autoFocus
                                fullWidth
                                className={classNames(
                                    "search-input-v1",
                                    classes.inputBase
                                )}
                                inputProps={{
                                    ...params.inputProps,
                                    // Search icon
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <SearchIcon
                                                color="action"
                                                fontSize="small"
                                            />
                                        </InputAdornment>
                                    ),
                                }}
                                placeholder={`Search ${props.label}`}
                                ref={params.InputProps.ref}
                            />
                        )}
                        renderTags={() => null}
                        size="small"
                        onChange={(_e, newValue) =>
                            newValue && filterSelect(newValue)
                        }
                        onClose={(_event, reason) => {
                            if (reason === "escape") {
                                handleClose();
                            }
                        }}
                    />
                </ClickAwayListener>
            </Popper>
        </>
    );
};
