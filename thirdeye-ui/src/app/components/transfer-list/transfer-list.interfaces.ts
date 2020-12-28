export interface TransferListProps<T> {
    fromList: T[];
    toList?: T[];
    fromLabel?: string;
    toLabel?: string;
    listItemTextFn: (item: T) => string; // Function that shall return text to be displayed for list item
    listItemKeyFn: (item: T) => string | number; // Function that shall return unique identifier for list item
    onChange?: (toList: T[]) => void;
}
