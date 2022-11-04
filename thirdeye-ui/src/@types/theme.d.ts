// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import {
    AlertClassKey,
    AlertProps,
    AutocompleteClassKey,
    AutocompleteProps,
} from "@material-ui/lab";

// Module declaration augmenting Material UI Core with Material UI Lab components
declare module "@material-ui/core/styles/props" {
    export interface ComponentsPropsList {
        MuiAlert: AlertProps;
        MuiAutocomplete: AutocompleteProps<
            unknown,
            boolean | undefined,
            boolean | undefined,
            boolean | undefined
        >;
    }
}

declare module "@material-ui/core/styles/overrides" {
    export interface ComponentNameToClassKey {
        MuiAlert: AlertClassKey;
        MuiAutocomplete: AutocompleteClassKey;
    }
}
