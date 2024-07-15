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

export const useNotificationDisplayV1Styles = makeStyles((theme) => ({
    buttonContainer: {
        display: "flex",
        width: "50%",
        justifyContent: "space-evenly",
        "& button": {
            backgroundColor: "#0c5394",
        },
    },
    notificationActionVisible: {
        display: "flex",
    },
    notificationActionHidden: {
        display: "none",
    },
    snackBarContainer: {
        maxWidth: "90vw",
        minWidth: "500px",
        overflowWrap: "break-word",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
    },
    container: {
        marginTop: theme.spacing(2),
        justifyContent: "center",
        display: "flex",
        width: "100%",
    },
}));
