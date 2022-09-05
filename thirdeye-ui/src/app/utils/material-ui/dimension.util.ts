/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
// Material-UI theme dimensions
export const Dimension = {
    WIDTH_BORDER_DEFAULT: 1,
    WIDTH_BORDER_BUTTON_OUTLINED: 2,
    WIDTH_SNACKBAR_DEFAULT: 420,
    WIDTH_DRAWER_DEFAULT: 290,
    WIDTH_DRAWER_MINIMIZED: 50,
    WIDTH_PAGE_CONTENTS_CENTERED: 960,
    MIN_WIDTH_MENU: 250,
    HEIGHT_INPUT_DEFAULT: 56,
    HEIGHT_INPUT_SMALL_DEFAULT: 37,
    RADIUS_BORDER_DEFAULT: 8,
    SPACING_GRID_DEFAULT: 2,
    // Visualizations
    WIDTH_VISUALIZATION_STROKE_DEFAULT: 1,
    WIDTH_VISUALIZATION_STROKE_CURRENT: 2,
    WIDTH_VISUALIZATION_STROKE_BASELINE: 3,
    WIDTH_VISUALIZATION_STROKE_ANOMALY_LINE: 2,
    DASHARRAY_VISUALIZATION_BASELINE: "4,4",
    DASHARRAY_VISUALIZATION_ANOMALY: "4,4",
    WIDTH_VISUALIZATION_STROKE_HOVER_MARKER: 1,
    DASHARRAY_VISUALIZATION_HOVER_MARKER: 2,
    RADIUS_VISUALIZATION_HOVER_MARKER: 5,
    RADIUS_VISUALIZATION_ANOMALY_MARKER: 4,
    HEIGHT_VISUALIZATION_LEGEND_GLYPH: 15,
    WIDTH_VISUALIZATION_LEGEND_GLYPH: 15,
} as const;
