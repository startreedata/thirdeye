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
import { AutocompleteProps } from "@material-ui/lab";
import { AutocompletePaper } from "../../components/autocomplete-paper-v1/autocomplete-paper-v1.component";

// Material UI theme style overrides for Autocomplete
export const autocompleteClassesV1 = {
    inputRoot: {
        minHeight: 50,
    },
};

// Material UI theme property overrides for Autocomplete
export const autocompletePropsV1: Partial<
    AutocompleteProps<
        unknown,
        boolean | undefined,
        boolean | undefined,
        boolean | undefined
    >
> = {
    clearOnBlur: true,
    noOptionsText: "",
    PaperComponent: AutocompletePaper,
};
