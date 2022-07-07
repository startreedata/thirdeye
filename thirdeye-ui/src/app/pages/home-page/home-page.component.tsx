/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Grid } from "@material-ui/core";
import ErrorIcon from "@material-ui/icons/Error";
import SettingsIcon from "@material-ui/icons/Settings";
import WifiTetheringIcon from "@material-ui/icons/WifiTethering";
import {
    PageContentsGridV1,
    PageV1,
    TileButtonIconV1,
    TileButtonTextV1,
    TileButtonV1,
} from "@startree-ui/platform-ui";
import { default as React, FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { PageHeader } from "../../components/page-header/page-header.component";
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
            </PageContentsGridV1>
        </PageV1>
    );
};
