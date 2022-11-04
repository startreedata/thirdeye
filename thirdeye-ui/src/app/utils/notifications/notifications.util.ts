import { isEmpty } from "lodash";
import { NotificationTypeV1 } from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";

export const notifyIfErrors = (
    requestStatus: ActionStatus,
    errorMessages: string[] | null | undefined,
    notify: (msgType: NotificationTypeV1, msg: string) => void,
    fallbackMsg: string
): void => {
    if (requestStatus !== ActionStatus.Error) {
        return;
    }

    if (!isEmpty(errorMessages)) {
        errorMessages?.map((msg) => notify(NotificationTypeV1.Error, msg));
    } else {
        notify(NotificationTypeV1.Error, fallbackMsg);
    }
};
