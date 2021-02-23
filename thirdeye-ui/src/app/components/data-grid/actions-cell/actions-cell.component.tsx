import { Box, Grid, IconButton } from "@material-ui/core";
import DeleteIcon from "@material-ui/icons/Delete";
import EditIcon from "@material-ui/icons/Edit";
import VisibilityIcon from "@material-ui/icons/Visibility";
import React, { FunctionComponent } from "react";
import { ActionsCellProps } from "./actions-cell.interfaces";

export const ActionsCell: FunctionComponent<ActionsCellProps> = (
    props: ActionsCellProps
) => {
    const handleViewDetails = (): void => {
        props.onViewDetails && props.onViewDetails(props.rowId);
    };

    const handleEdit = (): void => {
        props.onEdit && props.onEdit(props.rowId);
    };

    const handleDelete = (): void => {
        props.onDelete && props.onDelete(props.rowId);
    };

    return (
        <Box width="100%">
            <Grid container justify="flex-end" spacing={0}>
                {/* View details button */}
                {props.viewDetails && (
                    <Grid item>
                        <IconButton size="small" onClick={handleViewDetails}>
                            <VisibilityIcon fontSize="small" />
                        </IconButton>
                    </Grid>
                )}

                {/* Edit button */}
                {props.edit && (
                    <Grid item>
                        <IconButton size="small" onClick={handleEdit}>
                            <EditIcon fontSize="small" />
                        </IconButton>
                    </Grid>
                )}

                {/* Delete button */}
                {props.delete && (
                    <Grid item>
                        <IconButton size="small" onClick={handleDelete}>
                            <DeleteIcon fontSize="small" />
                        </IconButton>
                    </Grid>
                )}
            </Grid>
        </Box>
    );
};
