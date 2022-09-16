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
import { DialogDataV1, NotificationTypeV1 } from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";

export const promptDeleteConfirmation = (
    uiAnomalies: UiAnomaly[],
    callbackForOk: () => void,
    translate: (key: string, params?: { [key: string]: string }) => string,
    showDialog: (dialogParams: DialogDataV1) => void
): void => {
    let promptMsg = translate("message.delete-confirmation", {
        name: uiAnomalies[0].name,
    });

    if (uiAnomalies.length > 1) {
        promptMsg = translate("message.delete-confirmation", {
            name: `${uiAnomalies.length} ${translate("label.anomalies")}`,
        });
    }

    showDialog({
        type: DialogType.ALERT,
        contents: promptMsg,
        okButtonText: translate("label.confirm"),
        cancelButtonText: translate("label.cancel"),
        onOk: () => callbackForOk(),
    });
};

export const makeDeleteRequest = (
    uiAnomalies: UiAnomaly[],
    translate: (
        key: string,
        params?: { [key: string]: string | number }
    ) => string,
    notify: (type: NotificationTypeV1, msg: string) => void
): Promise<UiAnomaly[]> => {
    return Promise.allSettled(
        uiAnomalies.map((uiAnomaly) => deleteAnomaly(uiAnomaly.id))
    ).then((completedRequests) => {
        const successfullyRemoved: UiAnomaly[] = [];
        let numSuccessful = 0;
        let errored = 0;

        completedRequests.forEach((settled) => {
            if (settled.status === "fulfilled") {
                numSuccessful = numSuccessful + 1;
                successfullyRemoved.push(settled.value as unknown as UiAnomaly);
            } else {
                errored = errored + 1;
            }
        });

        if (uiAnomalies.length === 1 && numSuccessful === 1) {
            notify(
                NotificationTypeV1.Success,
                translate("message.delete-success", {
                    entity: translate("label.anomaly"),
                })
            );
        } else {
            if (numSuccessful > 0) {
                notify(
                    NotificationTypeV1.Success,
                    translate("message.num-delete-success", {
                        entity: translate("label.anomalies"),
                        num: numSuccessful,
                    })
                );
            }
            if (errored > 0) {
                notify(
                    NotificationTypeV1.Error,
                    translate("message.num-delete-error", {
                        entity: translate("label.anomalies"),
                        num: errored,
                    })
                );
            }
        }

        return successfullyRemoved;
    });
};
