// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";

export const useAppContainerV1Styles = makeStyles({
    appContainer: {
        // Container to occupy entire height and width so that contained components can be
        // horizontally/vertically centered if required
        height: "100%",
        width: "100%",
        display: "flex",
    },
});
