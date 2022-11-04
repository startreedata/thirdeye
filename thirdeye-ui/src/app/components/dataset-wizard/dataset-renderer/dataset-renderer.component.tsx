// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { DatasetRendererProps } from "./dataset-renderer.interfaces";

export const DatasetRenderer: FunctionComponent<DatasetRendererProps> = (
    props: DatasetRendererProps
) => {
    const { t } = useTranslation();

    return (
        <Grid container item justifyContent="flex-end">
            {/* Name */}
            <Grid item sm={2}>
                <Typography variant="subtitle1">
                    <strong>{t("label.name")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={10}>
                <Typography variant="body2">
                    {(props.dataset && props.dataset.name) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>

            {/* Associated Datasource */}
            <Grid item sm={2}>
                <Typography variant="subtitle1">
                    <strong>{t("label.associated-datasource")}</strong>
                </Typography>
            </Grid>

            <Grid item sm={10}>
                <Typography variant="body2">
                    {(props.dataset && props.dataset.dataSource?.name) ||
                        t("label.no-data-marker")}
                </Typography>
            </Grid>
        </Grid>
    );
};
