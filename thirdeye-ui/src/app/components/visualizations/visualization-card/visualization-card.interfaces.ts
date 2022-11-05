import { ReactNode } from "react";

export interface VisualizationCardProps {
    maximized?: boolean;
    visualizationHeight: number;
    visualizationMaximizedHeight?: number;
    title?: string; // Displayed only when maximized
    error?: boolean;
    helperText?: string; // Displayed only when maximized
    hideRefreshButton?: boolean; // Refresh button displayed only when maximized
    onRefresh?: () => void;
    onMaximize?: () => void;
    onRestore?: () => void;
    children: ReactNode;
}
