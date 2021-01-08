export interface ExpandableDetailsProps<T> {
    label: string;
    values: T[];
    expand?: boolean;
    link?: boolean;
    searchWords?: string[];
    valueTextFn: (value: T) => string;
    onLinkClick?: (value: T) => void;
    onChange?: (expand: boolean) => void;
}
