export interface DataGridExpandIconV1Props {
    depth: number;
    expandable: boolean;
    expanded: boolean;
    className?: string;
    onExpand: (expanded: boolean) => void;
    [key: string]: unknown;
}
