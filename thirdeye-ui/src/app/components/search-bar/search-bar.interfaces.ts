export interface SearchBarProps {
    searchText?: string;
    autoFocus?: boolean;
    searchLabel: string;
    searchStatusLabel?: string;
    setSearchQueryString?: boolean;
    onChange?: (searchWords: string[]) => void;
}

export enum SearchQueryStringKey {
    SEARCH = "search",
    SEARCH_TEXT = "searchText",
}
