/*
 * Copyright 2024 StarTree Inc
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

import React from "react";
import { Group } from "@visx/group";
import { BarGroup } from "@visx/shape";
import { AxisBottom, AxisLeft, AxisRight, AxisTop } from "@visx/axis";
import { ParentSize } from "@visx/responsive";
import { scaleBand, scaleLinear, scaleOrdinal } from "@visx/scale";
import { epochToDate } from "../../impact-dashboard/detection-performance/util";

import { ReactElement } from "react-markdown/lib/react-markdown";
import { TimeSeriesChartProps } from "../time-series-chart/time-series-chart.interfaces";
import { getMinMax } from "../time-series-chart/time-series-chart.utils";
import { Legend } from "../time-series-chart/legend/legend.component";

const blue = "#aeeef8";

export const green = "#F37B0E";
const purple = "#9caff6";

export const background = "#612efb";
const MIN_DATA_POINTS_TO_DISPLAY = 14;
const CHART_SEPARATION = 30;
const CHART_MARGINS = {
    top: 20,
    left: 50,
    bottom: 20,
    right: 50,
};

// export const BarGraph: FunctionComponent<TimeSeriesChartProps> = (
//     props
// ) => {
//     return (
//         <ParentSize>
//             {({ width, height }) => (
//                 <BarGraphInternal
//                     height={props.height || height}
//                     margins={CHART_MARGINS}
//                     width={width}
//                     {...props}
//                 />
//             )}
//         </ParentSize>
//     );
// };

// export const BarGraphInternal = (
// {data, keys, height, width, margins, LegendComponent=Legend}) => {
//     console.log('hw', height, width, data)
//     const xMax = width - margins.left - margins.right;
//   const yMax = height - margins.top - margins.bottom;
//   const formatDate = (date: number) => epochToDate(date)
//   const getDate = (d) => epochToDate(d.date);
//   const dateScale = scaleBand<string>({
//     domain: data.map(getDate),
//     padding: 0.1,
//   });
//   dateScale.rangeRound([0, xMax]);
//   const cityScale = scaleBand<string>({
//     domain: keys,
//     padding: 0.1,
//   });
//   cityScale.rangeRound([0, dateScale.bandwidth()]);
//   const tempScale = scaleLinear<number>({
//     domain: [0, Math.max(...data.map((d) => Math.max(...keys.map((key) => Number(d[key])))))],
//   });

//   tempScale.range([yMax, 0]);
//   const colorScale = scaleOrdinal<string, string>({
//     domain: keys,
//     range: ['#F37B0E','#006CA7'],
//   });
// //   dateScale.rangeRound([0, 600]);
// //   cityScale.rangeRound([0, dateScale.bandwidth()]);
// //   tempScale.range([500, 0]);
// //   const width = 500
// //   const height = 500
//   console.log('data', data)
//   return (
//     <>
//     <svg  width={width} height={height}>
//       {/* <rect width={'100%'} height={'1400'}/> */}
//     <Group top={margins.top} left={margins.left}>
//       <BarGroup
//         data={data}
//         keys={keys}
//         height={yMax}
//         x0={getDate}
//         x0Scale={dateScale}
//         x1Scale={cityScale}
//         yScale={tempScale}
//         color={colorScale}
//       >
//         {(barGroups) => {
//           return (
//             barGroups.map((barGroup) => (
//               <Group key={`bar-group-${barGroup.index}-${barGroup.x0}`} left={barGroup.x0}>
//                 {barGroup.bars.map((bar, idx) => (
//                   <rect
//                     key={`bar-group-bar-${barGroup.index}-${bar.index}-${bar.value}-${bar.key}`}
//                     x={bar.x}
//                     y={bar.y}
//                     width={bar.width}
//                     height={bar.height}
//                     fill={bar.color}
//                     rx={4}
//                     onClick={() => {
//                       // if (!events) return;
//                       const { key, value,x,y } = bar;
//                       alert(JSON.stringify({ key, value, y, x }));
//                     }}
//                   />
//                 ))}
//               </Group>
//             ))
//           )
//         }
//         }
//       </BarGroup>
//     </Group>
//     <AxisBottom
//       top={yMax + margins.top}
//       left={70}
//       // tickFormat={formatDate}
//       scale={dateScale}
//       stroke={'black'}
//     //   tickStroke={green}
//     //   hideAxisLine
//       tickLabelProps={{
//         fill: 'black',
//         fontSize: 11,
//         textAnchor: 'middle',
//       }}
//     />
//     <AxisRight
//       top={margins.top}
//       // tickFormat={formatDate}
//       left={xMax + margins.right}
//       scale={tempScale}
//       stroke={'black'}
//     //   tickStroke={'red'}
//     //   hideAxisLine
//       tickLabelProps={{
//         fill: 'black',
//         fontSize: 11,
//         textAnchor: 'start',
//       }}
//     />
//     </svg>
//                     </>
//   )
// }

export const BarGraph = ({ data, keys }: any): ReactElement => {
    return <></>;
};
