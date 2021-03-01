import { DataGridProps as MuiDataGridProps } from "@material-ui/data-grid";

export interface DataGridProps extends MuiDataGridProps {
    showToolbar?: boolean;
    searchWords?: string[];
    noDataAvailableMessage?: string;
}
