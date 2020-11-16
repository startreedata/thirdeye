import {
    Box,
    Card,
    Divider,
    Grid,
    makeStyles,
    Switch,
    Theme,
    Tooltip,
    Typography,
} from "@material-ui/core";
import EditIcon from "@material-ui/icons/Edit";
import FiberManualRecordIcon from "@material-ui/icons/FiberManualRecord";
import React, { ReactElement } from "react";
import { Alert } from "../../rest/dto/alert.interfaces";
import { AppRoute } from "../../utils/routes.util";
import { Button } from "../button/button.component";
import { RouterLink } from "../router-link/router-link.component";

type Props = {
    data: Alert;
    mode: "detail" | "list";
    onActiveChange?: (event: React.ChangeEvent, state: boolean) => void;
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

const AlertCard = ({
    data,
    mode = "list",
    onActiveChange,
}: Props): ReactElement => {
    const classes = useStyles();
    const {
        id,
        name,
        active,
        nodes: detections,
        owner: { principal },
    } = data;

    const detection = detections[Object.keys(detections).pop() || ""];

    return (
        <Card className={classes.root}>
            <Box display="flex" justifyContent="space-between">
                {mode === "list" ? (
                    <>
                        <Box display="flex">
                            <RouterLink to={`${id}`}>{name}</RouterLink>
                            &emsp;
                            {active && (
                                <Tooltip title="active">
                                    <FiberManualRecordIcon
                                        fontSize="small"
                                        htmlColor="green"
                                    />
                                </Tooltip>
                            )}
                            {/* Will add once get appropriate data from the API */}
                            {/* <Tooltip title="Health: 50%">
                        <DragHandleIcon fontSize="small" htmlColor="#2D9CDB" />
                    </Tooltip> */}
                        </Box>
                        <Tooltip title="Edit alert, Coming soon">
                            <RouterLink to={`/alerts/edit/${id}`}>
                                <EditIcon
                                    fontSize="small"
                                    style={{
                                        float: "right",
                                        color: "rgba(0, 0, 0, 0.54)",
                                    }}
                                />
                            </RouterLink>
                        </Tooltip>
                    </>
                ) : (
                    <Box alignItems="center" display="flex">
                        {active && (
                            <Tooltip title="active">
                                <>
                                    <FiberManualRecordIcon
                                        fontSize="small"
                                        htmlColor="green"
                                    />
                                    Active
                                </>
                            </Tooltip>
                        )}
                        <Switch
                            checked={active}
                            color="primary"
                            onChange={onActiveChange}
                        />
                        <RouterLink to={`${AppRoute.ALERTS_EDIT}/${id}`}>
                            <Button
                                color="primary"
                                startIcon={<EditIcon />}
                                variant="outlined"
                            >
                                Edit Alert
                            </Button>
                        </RouterLink>
                    </Box>
                )}
            </Box>
            <Grid container style={{ marginTop: "8px" }}>
                <Grid container item xs={12}>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Metric</strong>
                        </Typography>
                        <Typography variant="body1">
                            {detection &&
                            detection.metric &&
                            detection.metric.id
                                ? detection.metric.id
                                : "N/A"}
                        </Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Dataset</strong>
                        </Typography>
                        <Typography variant="body1">N/A</Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Application</strong>
                        </Typography>
                        <Typography variant="body1">N/A</Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Created by</strong>
                        </Typography>
                        <Typography variant="body1">{principal}</Typography>
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
                        <Typography variant="body1">{"N/A"}</Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Breakdown by</strong>
                        </Typography>
                        <Typography variant="body1">{"N/A"}</Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Detection Type</strong>
                        </Typography>
                        <Typography variant="body1">
                            {detection.type}
                        </Typography>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography variant="body1">
                            <strong>Subscription Group</strong>
                        </Typography>
                        <Typography variant="body1">{"N/A"}</Typography>
                    </Grid>
                </Grid>
            </Grid>
        </Card>
    );
};

export default AlertCard;
