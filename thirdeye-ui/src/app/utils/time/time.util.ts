// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
export const WEEK_IN_MILLISECONDS = 604800000;
export const DAY_IN_MILLISECONDS = 86400000;
export const YEAR_IN_MILLISECONDS = 31557600000;
export const MONTH_IN_MILLISECONDS = 2629800000;

export const OFFSET_TO_MILLISECONDS: { [key: string]: number } = {
    D: DAY_IN_MILLISECONDS,
    W: WEEK_IN_MILLISECONDS,
    M: MONTH_IN_MILLISECONDS,
    Y: YEAR_IN_MILLISECONDS,
};
