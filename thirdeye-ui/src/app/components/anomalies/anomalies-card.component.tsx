import {
    Box,
    Card,
    Grid,
    makeStyles,
    Theme,
    Typography,
} from "@material-ui/core";
import moment from "moment";
import React, { ReactElement } from "react";
import { useTranslation } from "react-i18next";
import { getAnomaliesDetailPath } from "../../utils/route/routes-util";
import { Button } from "../button/button.component";
import { RouterLink } from "../router-link/router-link.component";
import { AnomalyCardProps } from "./anomalies-card.interfaces";

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

const AnomaliesCard = ({ data, mode }: AnomalyCardProps): ReactElement => {
    const classes = useStyles();

    const { id, created, avgBaselineVal, endTime, avgCurrentVal, alert } = data;
    const { t } = useTranslation();

    return (
        <Card className={classes.root}>
            {mode === "list" ? (
                <Box
                    alignItems="center"
                    display="flex"
                    justifyContent="space-between"
                >
                    <Typography variant="subtitle2">
                        {t("label.id")}:{" "}
                        <RouterLink to={getAnomaliesDetailPath(id)}>
                            {id}
                        </RouterLink>
                    </Typography>
                    <Button color="primary" variant="outlined">
                        {t("label.investigate")}
                    </Button>
                </Box>
            ) : null}
            <Grid container style={{ marginTop: "8px" }}>
                <Grid container item xs={9}>
                    <Grid item xs={4}>
                        <Typography variant="body2">
                            <strong>{t("label.start")}</strong>
                        </Typography>
                        <Typography variant="body2">
                            {moment(created).format("MMM DD, h:mm A")}
                        </Typography>
                    </Grid>
                    <Grid item xs={4}>
                        <Typography variant="body2">
                            <strong>{t("label.duration")}</strong>
                        </Typography>
                        <Typography variant="body2">
                            {moment(endTime).from(created)}
                        </Typography>
                    </Grid>
                    <Grid item xs={4}>
                        <Typography variant="body2">
                            <strong>
                                {t("label.current")} / {t("label.predicted")}
                            </strong>
                        </Typography>
                        <Typography variant="body2">
                            {avgCurrentVal} / {avgBaselineVal}
                        </Typography>
                    </Grid>
                    <Grid item xs={4}>
                        <Typography variant="body2">
                            <strong>{t("entity.alert")}</strong>
                        </Typography>
                        <Typography variant="body2">{alert?.name}</Typography>
                    </Grid>
                    <Grid item xs={4}>
                        <Typography variant="body2">
                            <strong>{t("label.dimensions")}</strong>
                        </Typography>
                        <Typography variant="body2">-</Typography>
                    </Grid>
                </Grid>
                {mode === "detail" ? (
                    <Grid container item xs={3}>
                        <Grid item xs={12}>
                            <Button color="primary" variant="outlined">
                                {t("label.investigate")}
                            </Button>
                        </Grid>
                    </Grid>
                ) : null}
            </Grid>
        </Card>
    );
};

export default AnomaliesCard;
