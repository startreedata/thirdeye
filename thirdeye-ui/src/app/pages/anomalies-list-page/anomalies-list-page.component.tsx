import { Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { AnomalyListV1 } from "../../components/anomaly-list-v1/anomaly-list-v1.component";
import { PageContentsCardV1 } from "../../platform/components";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { getUiAnomalies } from "../../utils/anomalies/anomalies.util";
import { AnomaliesAllPageContext } from "../anomalies-all-page/anomalies-all-page.interfaces";

export const AnomaliesListAllPage: FunctionComponent = () => {
    const { anomalies, handleAnomalyDelete } =
        useOutletContext<AnomaliesAllPageContext>();
    const [uiAnomalies, setUiAnomalies] = useState<UiAnomaly[] | null>(null);

    useEffect(() => {
        if (anomalies && anomalies.length > 0) {
            setUiAnomalies(getUiAnomalies(anomalies));
        } else {
            setUiAnomalies([]);
        }
    }, [anomalies]);

    return (
        <Grid item xs={12}>
            {/* Anomaly list */}
            <PageContentsCardV1 disablePadding fullHeight>
                <AnomalyListV1
                    anomalies={uiAnomalies}
                    onDelete={handleAnomalyDelete}
                />
            </PageContentsCardV1>
        </Grid>
    );
};
