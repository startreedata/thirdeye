import { Card, Grid, MenuItem, Select, Typography } from "@material-ui/core";
import React from "react";
import { Link, withRouter } from "react-router-dom";
import AlertCard from "../../components/alerts/alert-card.component";
import { CustomBreadcrumbs } from "../../components/breadcrumbs/breadcrumbs.component";
import { PageContainer } from "../../components/containers/page-container.component";
import { cardStyles } from "../../components/styles/common.styles";
import { alerts } from "../../mock";
import { AppRoute } from "../../utils/routes.util";

export const AlertsDetailPage = withRouter((props) => {
    const { id } = props.match.params;
    // Enable this when we have api working
    // const { data: alert } = useAlert(id);

    const cardClasses = cardStyles();

    // Mock data
    const alert = alerts.find((a) => a.id === parseInt(id));

    if (!alert) {
        return <>LOADING</>;
    }

    const breadcrumbs = (
        <CustomBreadcrumbs>
            <Link to={AppRoute.ALERTS_ALL}>Alerts</Link>
            <Typography color="textPrimary">{alert.name}</Typography>
        </CustomBreadcrumbs>
    );

    return (
        <PageContainer breadcrumbs={breadcrumbs}>
            <Typography variant="h4">{alert.name}</Typography>
            <AlertCard data={alert} />
            <Card className={cardClasses.base}>
                <Link style={{ float: "right" }} to="#">
                    Report Missing Anomly
                </Link>
                <Typography variant="subtitle2">
                    All detection rules anomalies over time (0)
                </Typography>
                <Select disabled value="all" variant="outlined">
                    <MenuItem value="all">All Detection Rules</MenuItem>
                </Select>
                <Card className={cardClasses.base}>Chart</Card>
            </Card>
            <Card className={cardClasses.base}>
                <Grid container>
                    <Grid item xs={10}>
                        <Grid container>
                            <Grid item xs={12}>
                                <Typography variant="subtitle1">
                                    Alert Performance
                                </Typography>
                            </Grid>
                            <Grid item xs={12}>
                                <Grid container>
                                    <Grid item xs={3}>
                                        Annomalies
                                    </Grid>
                                    <Grid item xs={3}>
                                        Response Rate
                                    </Grid>
                                    <Grid item xs={3}>
                                        Precision
                                    </Grid>
                                    <Grid item xs={3}>
                                        Recall
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                    <Grid item xs={2}>
                        <Grid container>
                            <Grid item xs={12}>
                                Detection Health
                            </Grid>
                            <Grid item xs={12}>
                                30-day Status
                            </Grid>
                            <Grid item xs={12}>
                                Normal
                            </Grid>
                            <Grid item xs={12}>
                                View details
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </Card>
        </PageContainer>
    );
});
