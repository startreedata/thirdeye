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

import { makeStyles, Theme } from "@material-ui/core";
import { VisualizationCardProps } from "./visualization-card.interfaces";

const MARGIN_TOP_VISUALIZATION_CARD_MAXIMIZED = 100;
const MARGIN_VISUALIZATION_CARD_MAXIMIZED = 32;
const HEIGHT_VISUALIZATION_CARD_HEADER = 72;

export const useVisualizationCardStyles = makeStyles<
    Theme,
    VisualizationCardProps
>((theme) => ({
    visualizationCard: {
        height: (props) => props.visualizationHeight,
        willChange: "position, top, left, height, width, transform",
    },
    visualizationCardMaximized: {
        position: "fixed",
        top: "50%",
        left: "50%",
        height: (props) =>
            HEIGHT_VISUALIZATION_CARD_HEADER +
            (props.visualizationMaximizedHeight || props.visualizationHeight), // Required height
        maxHeight: `calc(100% - ${
            MARGIN_TOP_VISUALIZATION_CARD_MAXIMIZED +
            MARGIN_VISUALIZATION_CARD_MAXIMIZED
        }px)`, // Available maxmized height, available height - top and bottom margins
        width: `calc(100% - ${MARGIN_VISUALIZATION_CARD_MAXIMIZED * 2}px)`, // Available width - left and right margins
        overflowY: "auto",
        zIndex: theme.zIndex.drawer + 3, // Maximized visualization card above backdrop
        WebkitTransform: "translate(-50%, -50%)", // Center the maximized visualization card
        willChange: "position, top, left, height, width, transform",
    },
    visualizationCardHeader: {
        height: HEIGHT_VISUALIZATION_CARD_HEADER,
    },
    helperText: {
        paddingRight: theme.spacing(1),
    },
    visualizationCardContents: {
        height: (props) => props.visualizationHeight,
        padding: 0,
    },
    visualizationCardContentsMaximized: {
        height: (props) =>
            props.visualizationMaximizedHeight || props.visualizationHeight,
    },
}));
