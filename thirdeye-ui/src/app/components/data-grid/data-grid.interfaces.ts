import { DataGridProps as MuiDataGridProps } from "@material-ui/data-grid";

export interface DataGridProps extends MuiDataGridProps {
    rowSelectionCount?: number;
    noDataAvailableMessage?: string;
    searchWords?: string[];
}
