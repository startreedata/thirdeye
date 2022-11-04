export interface FilterOptionsAutoCompleteProps<FetchedDataType> {
    name: string;
    label: string;

    fetchOptions: () => Promise<FetchedDataType[]>;
    formatOptionFromServer: (rawOption: FetchedDataType) => FilterOption;
    selected: FilterOption | null;

    formatSelectedAfterOptionsFetch?: (
        selected: FilterOption,
        options: FilterOption[]
    ) => FilterOption;

    onSelectionChange: (option: FilterOption | null) => void;
}

export interface FilterOption {
    // To identify option uniquely
    id: string | number;
    label: string;
}
