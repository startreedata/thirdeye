/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { AnomalyListV1 } from "../../components/anomaly-list-v1/anomaly-list-v1.component";
import { PageContentsCardV1 } from "../../platform/components";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { useGetEnumerationItems } from "../../rest/enumeration-items/enumeration-items.actions";
import { getUiAnomalies } from "../../utils/anomalies/anomalies.util";
import { AnomaliesAllPageContext } from "../anomalies-all-page/anomalies-all-page.interfaces";

export const AnomaliesListAllPage: FunctionComponent = () => {
    const { anomalies, handleAnomalyDelete } =
        useOutletContext<AnomaliesAllPageContext>();
    const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[] | null>(null);
    const {
        enumerationItems,
        getEnumerationItems,
        status: enumerationItemsStatus,
    } = useGetEnumerationItems();

    useEffect(() => {
        if (anomalies && anomalies.length > 0) {
            setUiAnomalies(getUiAnomalies(anomalies));

            const hasEnumerationList = anomalies.filter(
                (a) => a.enumerationItem
            );

            if (hasEnumerationList.length > 0) {
                const ids = Array.from(
                    new Set(
                        hasEnumerationList.map(
                            (a) => a.enumerationItem?.id as number
                        )
                    )
                );

                getEnumerationItems({
                    ids,
                });
            }
        } else {
            setUiAnomalies([]);
        }
    }, [anomalies]);

    return (
        <Grid item xs={12}>
            {/* Anomaly list */}
            <PageContentsCardV1 disablePadding fullHeight>
                <AnomalyListV1
                    showEnumerationItem
                    anomalies={uiAnomalies}
                    enumerationItems={enumerationItems}
                    enumerationItemsStatus={enumerationItemsStatus}
                    onDelete={handleAnomalyDelete}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
