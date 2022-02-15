import { Grid, useTheme } from "@material-ui/core";
import { default as React, FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { ReactComponent as AlertIcon } from "../../../assets/images/alert.svg";
import { ReactComponent as AnomalyIcon } from "../../../assets/images/anomaly.svg";
import { ReactComponent as ConfigurationIcon } from "../../../assets/images/configuration.svg";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    PageContentsGridV1,
    PageV1,
    TileButtonIconV1,
    TileButtonTextV1,
    TileButtonV1,
} from "../../platform/components";
import {
    getAlertsPath,
    getAnomaliesPath,
    getConfigurationPath,
} from "../../utils/routes/routes.util";

export const HomePage: FunctionComponent = () => {
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const theme = useTheme();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    return (
        <PageV1>
            <PageHeader showCreateButton title={t("label.home")} />

            <PageContentsGridV1>
                {/* Alerts */}
                <Grid item>
                    <TileButtonV1 href={getAlertsPath()}>
                        <TileButtonIconV1>
                            <AlertIcon fill={theme.palette.primary.main} />
                        </TileButtonIconV1>
                        <TileButtonTextV1>{t("label.alerts")}</TileButtonTextV1>
                    </TileButtonV1>
                </Grid>

                {/* Anomalies */}
                <Grid item>
                    <TileButtonV1 href={getAnomaliesPath()}>
                        <TileButtonIconV1>
                            <AnomalyIcon fill={theme.palette.primary.main} />
                        </TileButtonIconV1>
                        <TileButtonTextV1>
                            {t("label.anomalies")}
                        </TileButtonTextV1>
                    </TileButtonV1>
                </Grid>

                {/* Configuration */}
                <Grid item>
                    <TileButtonV1 href={getConfigurationPath()}>
                        <TileButtonIconV1>
                            <ConfigurationIcon
                                fill={theme.palette.primary.main}
                            />
                        </TileButtonIconV1>
                        <TileButtonTextV1>
                            {t("label.configuration")}
                        </TileButtonTextV1>
                    </TileButtonV1>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
