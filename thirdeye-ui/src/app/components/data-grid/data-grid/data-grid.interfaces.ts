import { DataGridProps as MuiDataGridProps } from "@material-ui/data-grid";
import { ReactNode } from "react";

export interface DataGridProps extends MuiDataGridProps {
    searchWords?: string[];
    showToolbar?: boolean;
    toolbarItems?: ReactNode;
    noDataAvailableMessage?: string;
    errorMessage?: string;
    selectedStatusLabelFn?: (count: number) => string;
}
