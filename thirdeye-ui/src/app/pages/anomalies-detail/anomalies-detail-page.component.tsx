import { Card, Grid, Typography } from "@material-ui/core";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { withRouter } from "react-router-dom";
import AnomaliesCard from "../../components/anomalies/anomalies-card.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { cardStyles } from "../../components/styles/common.styles";
import { getAnomaly } from "../../rest/anomaly/anomaly-rest";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";

export const AnomaliesDetailPage = withRouter((props) => {
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { id } = props.match.params;
    const [anomaly, setAnomaly] = useState<Anomaly>();
    useEffect(() => {
        fetchAnomaly(parseInt(id));
    }, [id]);

    const fetchAnomaly = async (id: number): Promise<void> => {
        setAnomaly(await getAnomaly(id));
        setLoading(false);
    };
    const { t } = useTranslation();

    const cardClasses = cardStyles();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: "ANOMALY_NAME",
                path: "",
            },
        ]);
    }, [setPageBreadcrumbs, t]);

    if (loading || !anomaly) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <Typography variant="h5">Anomaly #{anomaly.id}</Typography>
            <Grid container spacing={2}>
                <Grid item xs={12}>
                    <Typography>
                        {t("label.metric")}:{" "}
                        <strong>{anomaly?.metric?.name}</strong>&emsp;
                        {t("label.dataset")}:{" "}
                        <strong>{anomaly?.metric?.dataset?.name}</strong>
                    </Typography>
                    <Typography />
                </Grid>
                <Grid item xs={12}>
                    <Card
                        className={cardClasses.base}
                        style={{ minHeight: 300 }}
                    >
                        Chart
                    </Card>
                </Grid>
                <AnomaliesCard data={anomaly} mode="detail" />
            </Grid>
        </PageContainer>
    );
});
