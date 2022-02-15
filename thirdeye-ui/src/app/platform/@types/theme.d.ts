// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
