import { Box, IconButton } from "@material-ui/core";
import { GridCellParams } from "@material-ui/data-grid";
import DeleteIcon from "@material-ui/icons/Delete";
import EditIcon from "@material-ui/icons/Edit";
import VisibilityIcon from "@material-ui/icons/Visibility";
import { isNil, toNumber } from "lodash";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { ActionsCellProps } from "./actions-cell.interfaces";

const ActionsCell: FunctionComponent<ActionsCellProps> = (
    props: ActionsCellProps
) => {
    const [rowId, setRowId] = useState(-1);
    const [align, setAlign] = useState("");

    useEffect(() => {
        setRowId(
            toNumber(props.params && props.params.row && props.params.row.id)
        );
        setAlign(
            props.params && props.params.colDef && props.params.colDef.align
        );
    }, []);

    const handleViewDetailsClick = (): void => {
        if (isNil(rowId) || rowId < 0) {
            return;
        }

        props.onViewDetails && props.onViewDetails(rowId);
    };

    const handleEditClick = (): void => {
        if (isNil(rowId) || rowId < 0) {
            return;
        }

        props.onEdit && props.onEdit(rowId);
    };

    const handleDeleteClick = (): void => {
        if (isNil(rowId) || rowId < 0) {
            return;
        }

        props.onDelete && props.onDelete(rowId);
    };

    return (
        <Box textAlign={align} width="100%">
            {/* View details button */}
            {props.viewDetails && (
                <IconButton size="small" onClick={handleViewDetailsClick}>
                    <VisibilityIcon fontSize="small" />
                </IconButton>
            )}

            {/* Edit button */}
            {props.edit && (
                <IconButton size="small" onClick={handleEditClick}>
                    <EditIcon fontSize="small" />
                </IconButton>
            )}

            {/* Delete button */}
            {props.delete && (
                <IconButton size="small" onClick={handleDeleteClick}>
                    <DeleteIcon fontSize="small" />
                </IconButton>
            )}
        </Box>
    );
};

export const actionsCellRenderer = (
    params: GridCellParams,
    showViewDetails?: boolean,
    showEdit?: boolean,
    showDelete?: boolean,
    onViewDetails?: (rowId: number) => void,
    onEdit?: (rowId: number) => void,
    onDelete?: (rowId: number) => void
): ReactElement => {
    return (
        <ActionsCell
            delete={showDelete}
            edit={showEdit}
            params={params}
            viewDetails={showViewDetails}
            onDelete={onDelete}
            onEdit={onEdit}
            onViewDetails={onViewDetails}
        />
    );
};
