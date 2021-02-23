import { DataGridProps as MuiDataGridProps } from "@material-ui/data-grid";

export interface DataGridProps extends MuiDataGridProps {
    noDataAvailableMessage?: string;
    searchWords?: string[];
}
