import { ReactNode } from "react";

export interface VisualizationCardProps extends VisualizationCardCommonProps {
    children?: ReactNode;
}

export interface VisualizationCardCommonProps {
    title?: string;
    maximizedTitle?: string;
    visualizationHeight: number;
    visualizationMaximizedHeight?: number;
    stale?: boolean;
    staleLabel?: string;
    showRefreshButton?: boolean;
    showMaximizeButton?: boolean;
    startMaximized?: boolean;
    onRefresh?: () => void;
}
