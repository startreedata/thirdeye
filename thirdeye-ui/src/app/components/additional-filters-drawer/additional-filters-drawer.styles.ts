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
import { makeStyles } from "@material-ui/core";
import { ColorV1 } from "../../platform/utils/material-ui/color.util";

export const useAdditonalFiltersDrawerStyles = makeStyles((theme) => ({
    drawerPaper: {
        backgroundColor: theme.palette.background.paper,
        maxWidth: 280,
        width: '100%'
    },
    header: {
        padding: '10px 12px',
    },
    content: {
        borderBottom: `1px solid ${ColorV1.Grey10}`,
        borderTop: `1px solid ${ColorV1.Grey10}`,
        padding: '20px 12px',
    },
    footer: {
        padding: '10px 12px',
        backgroundColor: ColorV1.Grey9
    },
    actionSecondary: {
        textTransform: 'none',
        backgroundColor: ColorV1.White1,
        border: `1px solid ${ColorV1.Grey10}`,
    },
    actionPrimary: {
        textTransform: 'none',
    }
}));
