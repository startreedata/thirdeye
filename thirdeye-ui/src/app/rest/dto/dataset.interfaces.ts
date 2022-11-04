// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Datasource } from "./datasource.interfaces";

export interface Dataset {
    id: number;
    name: string;
    active: boolean;
    additive: boolean;
    dimensions: string[];
    timeColumn: TimeColumn;
    expectedDelay: Duration;
    dataSource: Datasource;
}

export interface TimeColumn {
    name: string;
    interval: Duration;
    format: string;
    timezone: string;
}

export interface Duration {
    seconds: number;
    units: TemporalUnit[];
    nano: number;
    zero: boolean;
    negative: boolean;
}

export interface TemporalUnit {
    timeBased: boolean;
    numberBased: boolean;
    duration: Duration;
    durationEstimated: boolean;
}
