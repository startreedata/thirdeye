export interface TransferListProps<T> {
    fromList: T[];
    toList?: T[];
    fromLabel?: string;
    toLabel?: string;
    link?: boolean;
    loading?: boolean;
    listItemTextFn?: (listItem: T) => string; // Function that returns text to be displayed for list item
    listItemKeyFn?: (listItem: T) => string | number; // Function that returns unique identifier for list item
    onClick?: (listItem: T) => void;
    onChange?: (toList: T[]) => void;
}
