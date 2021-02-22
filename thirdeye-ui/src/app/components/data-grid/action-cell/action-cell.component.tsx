import { Box, Grid, IconButton } from "@material-ui/core";
import DeleteIcon from "@material-ui/icons/Delete";
import EditIcon from "@material-ui/icons/Edit";
import VisibilityIcon from "@material-ui/icons/Visibility";
import React, { FunctionComponent } from "react";
import { ActionCellProps } from "./action-cell.interfaces";

export const ActionCell: FunctionComponent<ActionCellProps> = (
    props: ActionCellProps
) => {
    const handleViewDetails = (): void => {
        props.onViewDetails && props.onViewDetails(props.id);
    };

    const handleEdit = (): void => {
        props.onEdit && props.onEdit(props.id);
    };

    const handleDelete = (): void => {
        props.onDelete && props.onDelete(props.id);
    };

    return (
        <Box width="100%">
            <Grid container justify="flex-end" spacing={0}>
                {/* View details button */}
                <Grid item>
                    <IconButton size="small" onClick={handleViewDetails}>
                        <VisibilityIcon />
                    </IconButton>
                </Grid>

                {/* Edit button */}
                <Grid item>
                    <IconButton size="small" onClick={handleEdit}>
                        <EditIcon />
                    </IconButton>
                </Grid>

                {/* Delete button */}
                <Grid item>
                    <IconButton size="small" onClick={handleDelete}>
                        <DeleteIcon />
                    </IconButton>
                </Grid>
            </Grid>
        </Box>
    );
};
