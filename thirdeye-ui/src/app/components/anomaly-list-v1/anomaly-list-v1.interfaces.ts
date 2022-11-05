import React from "react";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";

export interface AnomalyListV1Props {
    anomalies: UiAnomaly[] | null;
    onDelete?: (uiAnomalies: UiAnomaly[]) => void;
    searchFilterValue?: string | null;
    onSearchFilterValueChange?: (value: string) => void;
    toolbar?: React.ReactNode;
}
