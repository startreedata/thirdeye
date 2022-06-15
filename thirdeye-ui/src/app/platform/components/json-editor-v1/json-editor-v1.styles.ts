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

// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";
import { BorderV1 } from "../../utils/material-ui/border.util";
import { codeTypographyOptionsV1 } from "../../utils/material-ui/typography.util";

export const useJSONEditorV1Styles = makeStyles((theme) => ({
    jsonEditor: {
        height: "100%",
        width: "100%",
    },
    codeMirror: {
        height: "100%",
        width: "100%",
        border: BorderV1.BorderDefault,
        borderRadius: theme.shape.borderRadius,
        "& .CodeMirror": {
            height: "100%",
            width: "100%",
            ...codeTypographyOptionsV1.body2,
            borderRadius: theme.shape.borderRadius,
        },
        "& .CodeMirror-focused .CodeMirror-selected": {
            background: "highlight",
        },
        "& .CodeMirror-foldmarker": {
            color: theme.palette.primary.main,
            textShadow: "none",
        },
        "& .CodeMirror-matchingbracket": {
            color: `${theme.palette.primary.main} !important`,
            fontWeight: "bold",
        },
    },
    codeMirrorError: {
        borderColor: theme.palette.error.main,
    },
    validationIcon: {
        position: "absolute",
        top: 0,
        right: 0,
        margin: theme.spacing(1),
        zIndex: theme.zIndex.appBar - 1, // Validation icon to float above CodeMirror
    },
}));
