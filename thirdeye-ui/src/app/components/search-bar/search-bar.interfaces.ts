export interface SearchBarProps {
    autoFocus?: boolean;
    label?: string;
    searchStatusText?: string;
    setSearchQueryString?: boolean;
    onChange?: (searchWords: string[]) => void;
}
