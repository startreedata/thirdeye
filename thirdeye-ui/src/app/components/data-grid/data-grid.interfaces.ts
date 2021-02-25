import { DataGridProps as MuiDataGridProps } from "@material-ui/data-grid";

export interface DataGridProps extends MuiDataGridProps {
    searchWords?: string[];
    noDataAvailableMessage?: string;
}
