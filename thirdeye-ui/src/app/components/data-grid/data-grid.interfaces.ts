import { DataGridProps as MuiDataGridProps } from "@material-ui/data-grid";

export interface DataGridProps {
    searchWords: string[];
    dataGrid: MuiDataGridProps;
}
