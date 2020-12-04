export interface SearchProps {
    autoFocus?: boolean;
    searchStatusText?: string;
    onChange?: (searchWords: string[]) => void;
    syncSearchWithURL?: boolean;
}
