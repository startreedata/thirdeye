import { Grid } from "@material-ui/core";
import DescriptionIcon from "@material-ui/icons/Description";
import ErrorIcon from "@material-ui/icons/Error";
import SettingsIcon from "@material-ui/icons/Settings";
import WifiTetheringIcon from "@material-ui/icons/WifiTethering";
import { default as React, FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
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
    const { t } = useTranslation();

    return (
        <PageV1>
            <PageHeader showCreateButton title={t("label.home")} />

            <PageContentsGridV1>
                {/* Alerts */}
                <Grid item>
                    <TileButtonV1 href={getAlertsPath()}>
                        <TileButtonIconV1>
                            <WifiTetheringIcon color="primary" />
                        </TileButtonIconV1>
                        <TileButtonTextV1>{t("label.alerts")}</TileButtonTextV1>
                    </TileButtonV1>
                </Grid>

                {/* Anomalies */}
                <Grid item>
                    <TileButtonV1 href={getAnomaliesPath()}>
                        <TileButtonIconV1>
                            <ErrorIcon color="primary" />
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
                            <SettingsIcon color="primary" />
                        </TileButtonIconV1>
                        <TileButtonTextV1>
                            {t("label.configuration")}
                        </TileButtonTextV1>
                    </TileButtonV1>
                </Grid>

                {/* Documentation */}
                <Grid item>
                    <TileButtonV1
                        externalLink
                        href="https://dev.startree.ai/docs/thirdeye/"
                        target="_blank"
                    >
                        <TileButtonIconV1>
                            <DescriptionIcon color="primary" />
                        </TileButtonIconV1>
                        <TileButtonTextV1>
                            {t("label.documentation")}
                        </TileButtonTextV1>
                    </TileButtonV1>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
