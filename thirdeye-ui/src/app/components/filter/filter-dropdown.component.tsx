import { CircularProgress, TextField, Typography } from "@material-ui/core";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import { Autocomplete } from "@material-ui/lab";
import React, { ReactElement, useEffect, useState } from "react";
import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
} from "../styles/accordian.styles";

type Props = {
    label: string;
    labelProp: string;
    valueProp: string;
    fetchList?: (search: string) => Promise<ListOptions[]>;
    options?: ListOptions[];
    placeholder?: string;
};

export type ListOptions = {
    [name: string]: string | number;
};

const FilterDropdownComponent = ({
    fetchList,
    labelProp,
    valueProp,
    label,
    placeholder,
    options,
}: Props): ReactElement => {
    const [list, setList] = React.useState(options);
    const [loading, setLoading] = React.useState(false);
    const [search, setSearch] = useState("");
    const [expanded, setExpanded] = useState(true);

    useEffect(() => {
        async function fetchOptions(search: string): Promise<void> {
            setLoading(true);
            const options = fetchList && (await fetchList(search));
            setList(options || []);
            setLoading(false);
        }

        if (fetchList) {
            fetchOptions(search);
        }
    }, [search, fetchList]);

    return (
        <Accordion
            expanded={expanded}
            onChange={(): void => {
                return setExpanded(!expanded);
            }}
        >
            <AccordionSummary
                aria-controls="panel1bh-content"
                expandIcon={<ExpandMoreIcon />}
                id="panel1bh-header"
            >
                <Typography variant="subtitle1">{label}</Typography>
            </AccordionSummary>
            <AccordionDetails>
                {/* Since we don't have search with API */}
                {/* We can replace this Autocomplete with just Select */}
                <Autocomplete
                    fullWidth
                    getOptionLabel={(option): string => {
                        return option[labelProp] + "";
                    }}
                    getOptionSelected={(option, value): boolean => {
                        return option[valueProp] === value[valueProp];
                    }}
                    loading={loading}
                    options={list || []}
                    renderInput={(params): ReactElement => {
                        return (
                            <TextField
                                {...params}
                                InputProps={{
                                    ...params.InputProps,
                                    endAdornment: (
                                        <React.Fragment>
                                            {loading ? (
                                                <CircularProgress
                                                    color="inherit"
                                                    size={20}
                                                />
                                            ) : null}
                                            {params.InputProps.endAdornment}
                                        </React.Fragment>
                                    ),
                                }}
                                placeholder={placeholder}
                                variant="outlined"
                                onChange={(e): void => {
                                    return setSearch(e.target.value);
                                }}
                            />
                        );
                    }}
                    size="small"
                />
            </AccordionDetails>
        </Accordion>
    );
};

FilterDropdownComponent.defaultProps = {
    labelProp: "label",
    valueProp: "value",
    placeholder: "Search",
    options: [],
};

export default FilterDropdownComponent;
