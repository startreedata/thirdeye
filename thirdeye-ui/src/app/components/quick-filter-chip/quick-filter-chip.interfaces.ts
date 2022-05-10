export interface ChipFilterProps {
    name: string;
    label: string;
    options: Array<FilterOption>;
    value: FilterOption;
    onFilter: (filter?: FilterOption) => void;
}

export interface FilterOption {
    // To identify option uniquely
    id: string | number;
    label: string;
}
