import { Grid } from "@material-ui/core";
import { isEmpty, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { AnomalyCard } from "../../components/anomaly-card/anomaly-card.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { getAnomaly } from "../../rest/anomaly/anomaly-rest";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import {
    getAnomalyCardData,
    getAnomalyName,
} from "../../utils/anomaly/anomaly-util";
import { isValidNumberId } from "../../utils/params/params-util";
import { getAnomaliesDetailPath } from "../../utils/route/routes-util";
import { SnackbarOption } from "../../utils/snackbar/snackbar-util";
import { AnomaliesDetailPageParams } from "./anomalies-detail-page.interfaces";

export const AnomaliesDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [anomaly, setAnomaly] = useState<Anomaly>({} as Anomaly);
    const [setPageBreadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const params = useParams<AnomaliesDetailPageParams>();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    const fetchData = async (): Promise<void> => {
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.anomaly"),
                    id: params.id,
                }),
                SnackbarOption.ERROR
            );

            return;
        }

        let anomaly = {} as Anomaly;
        try {
            anomaly = await getAnomaly(toNumber(params.id));
        } catch (error) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } finally {
            setAnomaly(anomaly);

            // Create page breadcrumbs
            setPageBreadcrumbs([
                {
                    text: getAnomalyName(anomaly),
                    path: getAnomaliesDetailPath(anomaly.id),
                },
            ]);
        }
    };

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents centerAlign title={getAnomalyName(anomaly)}>
                {!isEmpty(anomaly) && (
                    <Grid container>
                        <Grid item md={12}>
                            <AnomalyCard
                                hideViewDetailsLinks
                                anomaly={getAnomalyCardData(anomaly)}
                            />
                        </Grid>
                    </Grid>
                )}
            </PageContents>
        </PageContainer>
    );
};
