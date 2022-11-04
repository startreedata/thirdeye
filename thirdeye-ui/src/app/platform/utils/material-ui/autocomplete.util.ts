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
