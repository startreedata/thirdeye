// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { makeStyles } from "@material-ui/core";
import { DimensionV1 } from "../../../../utils/material-ui/dimension.util";

export const usePageContentsGridV1Styles = makeStyles((theme) => ({
    pageContentsGrid: {
        minWidth: 300,
        display: "flex",
        flexDirection: "column",
        paddingBottom: theme.spacing(DimensionV1.PageGridSpacing),
        paddingLeft: theme.spacing(DimensionV1.PageGridSpacing),
        paddingRight: theme.spacing(DimensionV1.PageGridSpacing),
    },
    pageContentsGridFullHeight: {
        flex: 1,
    },
}));
