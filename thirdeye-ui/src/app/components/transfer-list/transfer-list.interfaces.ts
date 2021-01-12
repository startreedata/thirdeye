export interface TransferListProps<T> {
    fromList: T[];
    toList?: T[];
    fromLabel?: string;
    toLabel?: string;
    listItemTextFn: (item: T) => string; // Function that returns text to be displayed for list item
    listItemKeyFn: (item: T) => string | number; // Function that returns unique identifier for list item
    onChange?: (toList: T[]) => void;
}
