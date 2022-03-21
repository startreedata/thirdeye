import { GridCellParams } from "@material-ui/data-grid";
import { ReactNode } from "react";

export interface CustomCellProps {
    params: GridCellParams;
    children?: ReactNode;
}
