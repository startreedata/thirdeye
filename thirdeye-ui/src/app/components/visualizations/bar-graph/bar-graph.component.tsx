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
// import { Group } from '@visx/group';
// import { BarGroup } from '@visx/shape';
// import { AxisBottom, AxisLeft, AxisRight, AxisTop } from '@visx/axis';
// import { scaleBand, scaleLinear, scaleOrdinal } from '@visx/scale';
// import { epochToDate } from "../../impact-dashboard/detection-performance/util";

import { ReactElement } from "react-markdown/lib/react-markdown";

// const blue = '#aeeef8';
// export const green = '#e5fd3d';
// const purple = '#9caff6';
// export const background = '#612efb';

// const BarGraph = ({data, keys}) => {
//   const formatDate = (date: number) => epochToDate(date)
//   const getDate = (d) => epochToDate(d.date);
//   const dateScale = scaleBand<string>({
//     domain: data.map(getDate),
//     padding: 0.1,
//   });
//   const cityScale = scaleBand<string>({
//     domain: keys,
//     padding: 0.1,
//   });
//   const tempScale = scaleLinear<number>({
//     domain: [0, Math.max(...data.map((d) => Math.max(...keys.map((key) => Number(d[key])))))],
//   });
//   const colorScale = scaleOrdinal<string, string>({
//     domain: keys,
//     range: ['red', 'green'],
//   });
//   dateScale.rangeRound([0, 600]);
//   cityScale.rangeRound([0, dateScale.bandwidth()]);
//   tempScale.range([500, 0]);
//   const width = 475
//   const height = 475
//   return (
//     <svg  width={width} height={height}>
//       <rect x={0} y={0} width={width} height={height}  rx={14} />
//     <Group>
//       <BarGroup
//         data={data}
//         keys={keys}
//         height={1400}
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
//       top={450}
//       // tickFormat={formatDate}
//       scale={dateScale}
//       stroke={green}
//       tickStroke={green}
//       // hideAxisLine
//       tickLabelProps={{
//         fill: green,
//         fontSize: 11,
//         textAnchor: 'middle',
//       }}
//     />
//     <AxisRight
//       top={100}
//       // tickFormat={formatDate}
//       scale={tempScale}
//       stroke={green}
//       tickStroke={green}
//       hideAxisLine
//       tickLabelProps={{
//         fill: green,
//         fontSize: 11,
//         textAnchor: 'middle',
//       }}
//     />
//     </svg>
//   )
// }

// export default BarGraph
const BarGraph = ({ data, keys }: any): ReactElement => {
    return <></>;
};

export default BarGraph;
