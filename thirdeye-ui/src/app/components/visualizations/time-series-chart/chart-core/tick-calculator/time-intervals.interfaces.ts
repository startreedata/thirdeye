/*
 * Copyright 2023 StarTree Inc
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

import { DateTime } from "luxon";

export interface TimeInterval {
    (d: DateTime): DateTime;
    floor: (d: DateTime) => DateTime;
    ceil: (d: DateTime) => DateTime;
    offset: (d: DateTime, step: number) => DateTime;
    range: (start: DateTime, stop: DateTime, step?: number) => DateTime[];
    filter: (testFunc: (date: DateTime) => boolean) => TimeInterval;
    count: (start: DateTime, end: DateTime) => number;
    every: (step: number) => TimeInterval;
}
