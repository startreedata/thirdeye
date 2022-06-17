///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

import { makeStyles } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesTooltipStyles = makeStyles(
    (theme) => ({
        alertEvaluationTimeSeriesTooltip: {
            minWidth: 140,
        },
        time: {
            marginBottom: theme.spacing(1),
        },
        nameValueContents: {
            width: "100%",
            display: "table",
        },
        name: {
            display: "table-cell",
            verticalAlign: "middle",
        },
        value: {
            display: "table-cell",
            textAlign: "right",
            verticalAlign: "middle",
            paddingLeft: theme.spacing(1),
        },
        anomaly: {
            marginTop: theme.spacing(1),
            marginBottom: theme.spacing(1),
        },
        more: {
            marginTop: theme.spacing(1),
        },
    })
);
