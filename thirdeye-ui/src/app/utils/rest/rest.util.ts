import { AxiosError } from "axios";
import { get, isEmpty } from "lodash";

export const getErrorMessages = (error: AxiosError): string[] => {
    const errMsgs: string[] = [];

    const errList = get(error, "response.data.list", []);

    if (Array.isArray(errList)) {
        errList.map((err: { code: string; msg: string }) => {
            // Toast error message
            if (!isEmpty(err.msg)) {
                errMsgs.push(err.msg);
            }
        });
    }

    return errMsgs;
};
