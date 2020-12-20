export interface SearchBarProps {
    autoFocus?: boolean;
    label?: string;
    searchStatusLabel?: string;
    setSearchQueryString?: boolean;
    onChange?: (searchWords: string[]) => void;
}
