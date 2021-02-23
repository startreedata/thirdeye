import { CellParams } from "@material-ui/data-grid";
import { ReactNode } from "react";

export interface CustomCellProps {
    params: CellParams;
    children?: ReactNode;
}
