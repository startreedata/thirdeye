import {
    Box,
    Card,
    Divider,
    Grid,
    makeStyles,
    Theme,
    Tooltip,
    Typography,
} from "@material-ui/core";
import DragHandleIcon from "@material-ui/icons/DragHandle";
import EditIcon from "@material-ui/icons/Edit";
import FiberManualRecordIcon from "@material-ui/icons/FiberManualRecord";
import React, { ReactElement } from "react";
import { Link } from "react-router-dom";
import { Alert } from "../../utils/rest/alerts-rest/alerts-rest.interfaces";

type Props = {
    data: Alert;
};

const useStyles = makeStyles((them: Theme) => {
    return {
        root: {
            margin: them.spacing(2, 0),
            padding: them.spacing(1, 2),
            boxShadow: "none",
            border: "1px solid #BDBDBD",
            borderRadius: "8px",
        },
    };
});

const AlertCard = ({ data }: Props): ReactElement => {
    const classes = useStyles();
    const {
        id,
        breakdownBy,
        createdBy,
        datasetNames,
        application,
        filters,
        metric,
        name,
        subscriptionGroup,
        rules,
    } = data;

    const detection = rules[0]?.detection;

    return (
        <Card className={classes.root}>
            <Box display="flex" justifyContent="space-between">
                <Box display="flex">
                    <Link to={`${id}`}>
                        <Typography color="primary" variant="subtitle2">
                            <strong>{name}</strong>&nbsp;
                        </Typography>
                    </Link>
                    <Tooltip title="active">
                        <FiberManualRecordIcon
                            fontSize="small"
                            htmlColor="green"
                        />
                    </Tooltip>
                    <Tooltip title="Health: 50%">
                        <DragHandleIcon fontSize="small" htmlColor="#2D9CDB" />
                    </Tooltip>
                </Box>
                <Tooltip title="Edit alert">
                    <Link color="inherit" to={`/alerts/edit/${id}`}>
                        <EditIcon fontSize="small" style={{ float: "right" }} />
                    </Link>
                </Tooltip>
            </Box>
            <Grid container style={{ marginTop: "8px" }}>
                <Grid container item xs={12}>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Metric</strong>
                        </Typography>
                        <Typography variant="body1">{metric}</Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Dataset</strong>
                        </Typography>
                        <Typography variant="body1">{datasetNames}</Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Application</strong>
                        </Typography>
                        <Typography variant="body1">{application}</Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Created by</strong>
                        </Typography>
                        <Typography variant="body1">{createdBy}</Typography>
                    </Grid>
                </Grid>
                <Grid container item xs={12}>
                    <Divider style={{ width: "100%", margin: "8px 0" }} />
                </Grid>
                <Grid container item xs={12}>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Filtered by</strong>
                        </Typography>
                        <Typography variant="body1">
                            {filters || "N/A"}
                        </Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Breakdown by</strong>
                        </Typography>
                        <Typography variant="body1">
                            {breakdownBy || "N/A"}
                        </Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Detection Type</strong>
                        </Typography>
                        <Typography variant="body1">
                            {detection.map((d) => d.name)}
                        </Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Subscription Group</strong>
                        </Typography>
                        <Typography variant="body1">
                            {subscriptionGroup}
                        </Typography>
                    </Grid>
                </Grid>
            </Grid>
        </Card>
    );
};

export default AlertCard;
