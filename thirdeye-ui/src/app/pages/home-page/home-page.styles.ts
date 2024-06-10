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
import { createTheme, makeStyles } from "@material-ui/core";
import { lightV1, PaletteV1 } from "../../platform/utils";
import { ColorV1 } from "../../platform/utils/material-ui/color.util";
import { linkClassesV1 } from "../../platform/utils/material-ui/link.util";
import { paletteOptionsV1 } from "../../platform/utils/material-ui/palette.util";

export const useHomePageStyles = makeStyles((theme) => ({
    noDataIndicator: {
        height: 72.5,
    },
    page: {
        backgroundColor: PaletteV1.WhiteColor,
    },
    icon: {
        color: PaletteV1.NavBarBackgroundColor,
        fontSize: theme.typography.h5.fontSize,
    },
    iconGridContainer: {
        display: "flex",
        alignItems: "center",
    },
    gridSpace: {
        marginTop: theme.spacing(5),
    },
}));

export const homePageTheme = createTheme({
    ...lightV1,
    palette: {
        ...paletteOptionsV1,
        background: {
            ...paletteOptionsV1.background,
            default: PaletteV1.WhiteColor,
        },
    },
    typography: {
        ...lightV1.typography,
        h1: {
            ...lightV1.typography.h1,
            fontWeight: 700,
        },
        h2: {
            ...lightV1.typography.h2,
            fontWeight: 700,
        },
        h3: {
            ...lightV1.typography.h3,
            fontWeight: 700,
        },
        h4: {
            ...lightV1.typography.h4,
            fontWeight: 700,
        },
        h5: {
            ...lightV1.typography.h5,
            fontWeight: 700,
        },
        h6: {
            ...lightV1.typography.h6,
            fontWeight: 700,
        },
    },
    overrides: {
        ...lightV1.overrides,
        MuiLink: {
            ...linkClassesV1,
            root: {
                ...linkClassesV1.root,
                cursor: "pointer",
                color: PaletteV1.LinkColorDark,
                display: "flex",
                alignItems: "center",
            },
        },
        MuiTableCell: {
            root: {
                paddingTop: 8,
                paddingBottom: 8,
                borderBottom: 0,
            },
        },
        MuiTableRow: {
            root: {
                borderBottom: `1px solid ${PaletteV1.BorderColorGrey}`,
            },
        },
        MuiButton: {
            outlinedPrimary: {
                border: `solid 1px ${ColorV1.Blue9}`,
                backgroundColor: ColorV1.Grey9,
                color: PaletteV1.NavBarBackgroundColor,
                fontWeight: 500,
                borderRadius: lightV1.spacing(1),
                textTransform: "capitalize",
            },
        },
        // Override button styles
    },
});
