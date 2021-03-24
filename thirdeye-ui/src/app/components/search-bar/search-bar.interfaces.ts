export interface SearchBarProps {
    searchText?: string;
    autoFocus?: boolean;
    searchLabel: string;
    searchStatusLabel?: string;
    setSearchQueryString?: boolean;
    onChange?: (searchWords: string[]) => void;
}

export enum SearchQueryStringKey {
    SEARCH = "SEARCH",
    SEARCH_TEXT = "SEARCH_TEXT",
}
