export interface SearchBarProps {
    autoFocus?: boolean;
    searchLabel?: string;
    searchStatusLabel?: string;
    setSearchQueryString?: boolean;
    onChange?: (searchWords: string[]) => void;
}
