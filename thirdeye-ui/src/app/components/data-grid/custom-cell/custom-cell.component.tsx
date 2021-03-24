import { Box } from "@material-ui/core";
import { GridCellParams } from "@material-ui/data-grid";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { CustomCellProps } from "./custom-cell.interfaces";
import { useCustomCellStyles } from "./custom-cell.styles";

const CustomCell: FunctionComponent<CustomCellProps> = (
    props: CustomCellProps
) => {
    const customCellClasses = useCustomCellStyles();
    const [align, setAlign] = useState("");

    useEffect(() => {
        setAlign(
            props.params && props.params.colDef && props.params.colDef.align
        );
    }, []);

    return (
        <Box textAlign={align} width="100%">
            <div className={customCellClasses.cellContents}>
                {props.children}
            </div>
        </Box>
    );
};

export function customCellRenderer(
    params: GridCellParams,
    componentRenderer: (params: GridCellParams) => ReactElement
): ReactElement {
    return (
        <CustomCell params={params}>
            {componentRenderer && componentRenderer(params)}
        </CustomCell>
    );
}
