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
import { BorderV1 } from "../../utils/material-ui/border.util";
import { codeTypographyOptionsV1 } from "../../utils/material-ui/typography.util";
import { ColorV1 } from "../../utils/material-ui/color.util";

export const useJSONEditorV2Styles = makeStyles((theme) => ({
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
    footer: {
        border: `1px solid ${ColorV1.Grey10}`,
        overflow: "hidden",
        backgroundColor: ColorV1.Grey9,
        padding: "8px 12px",
        display: "flex",
        justifyContent: "space-between",

        '& > button': {
            backgroundColor: ColorV1.White1,
            border: `1px solid ${ColorV1.Grey10}`,
            borderRadius: 8
        }
    },
}));
