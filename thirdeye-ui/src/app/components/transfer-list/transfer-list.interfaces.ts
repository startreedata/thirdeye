export interface TransferListProps<T> {
    fromList: T[];
    toList: T[];
    title?: string;
    fromLabel?: string;
    toLabel?: string;
    renderer: (item: T) => string; // Function that shall return text to be displayed for given item in the list
    getKey: (item: T) => string | number; // Function that shall return a unique identifier for the given item
    onChange?: (toList: T[]) => void;
}
