import { AxiosError } from "axios";
import { isEmpty } from "lodash";

export const getErrorMessage = (error: AxiosError): string => {
    const errMsgs: string[] = [];

    if (
        error &&
        error.response &&
        error.response.data &&
        error.response.data.list.length
    ) {
        error.response.data.list.map((err: { code: string; msg: string }) => {
            // Toast error message
            if (!isEmpty(err.msg)) {
                errMsgs.push(err.msg);
            }
        });
    }

    // Remove this `0` index value if we need all the errors
    return errMsgs[0];
};
