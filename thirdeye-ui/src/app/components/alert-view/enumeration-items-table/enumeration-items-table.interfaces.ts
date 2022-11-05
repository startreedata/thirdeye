import { DataGridSortOrderV1 } from "../../../platform/components";
import { DetectionEvaluationForRender } from "../enumeration-item-merger/enumeration-item-merger.interfaces";

export interface EnumerationItemsTableProps {
    detectionEvaluations: DetectionEvaluationForRender[];
    expanded: string[];
    onExpandedChange: (newExpanded: string[]) => void;
    alertId: number;
    sortOrder: DataGridSortOrderV1;
    initialSearchTerm: string;
    onSearchTermChange: (newTerm: string) => void;
    onSortOrderChange: (newOrder: DataGridSortOrderV1) => void;
}
