import { UiAnomaly } from "../../../../rest/dto/ui-anomaly.interfaces";

export interface AnomalySummaryCardProps {
    uiAnomaly: UiAnomaly | null;
    isLoading?: boolean;
    className?: string;
}
