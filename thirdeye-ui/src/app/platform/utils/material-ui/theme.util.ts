/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { createTheme, Theme } from "@material-ui/core";
import { alertClassesV1, alertPropsV1 } from "./alert.util";
import {
    autocompleteClassesV1,
    autocompletePropsV1,
} from "./autocomplete.util";
import {
    buttonClassesV1,
    buttonGroupPropsV1,
    buttonPropsV1,
} from "./button.util";
import { cardClassesV1, cardContentClassesV1, cardPropsV1 } from "./card.util";
import { chipClassesV1, chipPropsV1 } from "./chip.util";
import { cssBaselineClassesV1 } from "./css-baseline.util";
import {
    dialogActionsClassesV1,
    dialogClassesV1,
    dialogContentClassesV1,
} from "./dialog.util";
import { formControlLabelClassesV1 } from "./form-control-label.util";
import { gridPropsV1 } from "./grid.util";
import { inputBaseClassesV1 } from "./input-base.util";
import { linkClassesV1, linkPropsV1 } from "./link.util";
import { listItemTextClassesV1 } from "./list.util";
import { menuItemClassesV1, menuPropsV1 } from "./menu.util";
import { outlinedInputClassesV1 } from "./outlined-input.util";
import { paletteOptionsV1 } from "./palette.util";
import { popoverClassesV1, popoverPropsV1 } from "./popover.util";
import { radioPropsV1 } from "./radio.util";
import { shapeOptionsV1 } from "./shape.util";
import { switchPropsV1 } from "./switch.util";
import { tablePaginationClassesV1 } from "./table-pagination.util";
import { tabClassesV1, tabsClassesV1, tabsPropsV1 } from "./tabs.util";
import { textFieldPropsV1 } from "./text-field.util";
import { typographyOptionsV1 } from "./typography.util";

// Material UI theme
export const lightV1: Theme = createTheme({
    palette: paletteOptionsV1,
    props: {
        MuiAlert: alertPropsV1,
        MuiAutocomplete: autocompletePropsV1,
        MuiButton: buttonPropsV1,
        MuiButtonGroup: buttonGroupPropsV1,
        MuiCard: cardPropsV1,
        MuiChip: chipPropsV1,
        MuiGrid: gridPropsV1,
        MuiLink: linkPropsV1,
        MuiMenu: menuPropsV1,
        MuiPopover: popoverPropsV1,
        MuiRadio: radioPropsV1,
        MuiSwitch: switchPropsV1,
        MuiTabs: tabsPropsV1,
        MuiTextField: textFieldPropsV1,
    },
    overrides: {
        MuiAlert: alertClassesV1,
        MuiAutocomplete: autocompleteClassesV1,
        MuiButton: buttonClassesV1,
        MuiCard: cardClassesV1,
        MuiCardContent: cardContentClassesV1,
        MuiCssBaseline: cssBaselineClassesV1,
        MuiChip: chipClassesV1,
        MuiDialog: dialogClassesV1,
        MuiDialogActions: dialogActionsClassesV1,
        MuiDialogContent: dialogContentClassesV1,
        MuiFormControlLabel: formControlLabelClassesV1,
        MuiInputBase: inputBaseClassesV1,
        MuiLink: linkClassesV1,
        MuiListItemText: listItemTextClassesV1,
        MuiMenuItem: menuItemClassesV1,
        MuiOutlinedInput: outlinedInputClassesV1,
        MuiPopover: popoverClassesV1,
        MuiTab: tabClassesV1,
        MuiTablePagination: tablePaginationClassesV1,
        MuiTabs: tabsClassesV1,
    },
    shape: shapeOptionsV1,
    typography: typographyOptionsV1,
});
