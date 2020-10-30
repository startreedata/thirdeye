import { ReactElement } from 'react'

import React from 'react';
import { Box, Card, Divider, Grid, makeStyles, Theme, Typography } from '@material-ui/core';
import FiberManualRecordIcon from '@material-ui/icons/FiberManualRecord';
import DragHandleIcon from '@material-ui/icons/DragHandle';
import EditIcon from '@material-ui/icons/Edit';

type Props = {
    data: AlertData
}

export type AlertData = {
    application: string,
    breakdownBy: string,
    createdBy: string,
    dataset: string,
    detectionType: string,
    filteredBy: string,
    metric: string,
    name: string,
    subscriptionGroup: string,
}

const useStyles = makeStyles((them: Theme) => {
    return {
        root: {
            margin: them.spacing(2, 2, 2, 3),
            padding: them.spacing(1, 2),
            boxShadow: 'none',
            border: '1px solid #BDBDBD',
            borderRadius: '8px',
        }
    }
})

const AlertCard = ({data}: Props): ReactElement => {
    
    const classes = useStyles();
    const {
        breakdownBy,
        createdBy,
        dataset,
        application,
        detectionType,
        filteredBy,
        metric,
        name,
        subscriptionGroup,
    } = data;

    return <Card className={classes.root} >
        <Box display="flex" justifyContent="space-between" >
            <Box display="flex">
                <Typography color="primary" variant="subtitle2"><strong>{name}</strong>&nbsp;</Typography>
                <FiberManualRecordIcon fontSize="small" htmlColor="green" />
                <DragHandleIcon fontSize="small" htmlColor="#2D9CDB" />
            </Box>
            <EditIcon fontSize="small" htmlColor="background: rgba(0, 0, 0, 0.54)" style={{float: 'right'}}  />
        </Box>
        <Grid container style={{marginTop: '8px'}} >
            <Grid container item xs={12}>
                <Grid item xs={3}>
                    <Typography variant="body1"><strong>Metric</strong></Typography>
                    <Typography variant="body1">{metric}</Typography>
                </Grid>
                <Grid item xs={3}>
                    <Typography variant="body1"><strong>Dataset</strong></Typography>
<Typography variant="body1">{dataset}</Typography>
                </Grid>
                <Grid item xs={3}>
                    <Typography variant="body1"><strong>Application</strong></Typography>
                    <Typography variant="body1">{application}</Typography>
                </Grid>
                <Grid item xs={3}>
                    <Typography variant="body1"><strong>Created  by</strong></Typography>
                    <Typography variant="body1">{createdBy}</Typography>
                </Grid>
            </Grid>
            <Grid container item xs={12}>
                <Divider style={{width: '100%', margin: '8px 0'}} />
            </Grid>
            <Grid container item xs={12}>
                <Grid item xs={3}>
                    <Typography variant="body1"><strong>Filtered by</strong></Typography>
                    <Typography variant="body1">{filteredBy || 'N/A'}</Typography>
                </Grid>
                <Grid item xs={3}>
                    <Typography variant="body1"><strong>Breakdown by</strong></Typography>
                    <Typography variant="body1">{breakdownBy || 'N/A'}</Typography>
                </Grid>
                <Grid item xs={3}>
                    <Typography variant="body1"><strong>Detection Type</strong></Typography>
                    <Typography variant="body1">{detectionType}</Typography>
                </Grid>
                <Grid item xs={3}>
                    <Typography variant="body1"><strong>Subscription Group</strong></Typography>
                    <Typography variant="body1">{subscriptionGroup}</Typography>
                </Grid>
            </Grid>
        </Grid>
    </Card>
}

export default AlertCard;