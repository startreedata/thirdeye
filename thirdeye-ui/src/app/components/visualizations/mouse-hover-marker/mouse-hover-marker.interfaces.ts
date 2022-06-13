// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

import { ProvidedZoom, TransformMatrix } from "@visx/zoom/lib/types";
import { ScaleLinear, ScaleTime } from "d3-scale";
import { MouseEvent, ReactNode } from "react";

// Zoom props
export interface ZoomProps {
    svgHeight: number;
    svgWidth: number;
    zoomHeight: number;
    zoomWidth: number;
    xAxisOnly?: boolean;
    yAxisOnly?: boolean;
    initialTransform: TransformMatrix;
    onChange?: (zoom?: VisxZoomProps) => void;
    children: (zoom: VisxZoomProps) => ReactNode;
}

export interface ZoomState {
    initialTransformMatrix: TransformMatrix;
    transformMatrix: TransformMatrix;
    isDragging: boolean;
}

export type VisxZoomProps = ProvidedZoom<unknown> & ZoomState;

export interface MouseHoverMarkerProps {
    x?: number;
    y?: number;
    xScale: ScaleTime<number, number>;
    yScale: ScaleLinear<number, number>;
    zoom?: VisxZoomProps;
    onMouseMove: (event: MouseEvent<SVGRectElement>) => void;
    onMouseLeave: (event: MouseEvent<SVGRectElement>) => void;
    onZoomChange?: (zoom: VisxZoomProps) => void;
    onMouseClick?: (event: MouseEvent<SVGRectElement>) => void;
    cursor?: string;
}
