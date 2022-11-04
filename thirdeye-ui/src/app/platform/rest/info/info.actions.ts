import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import { ActionStatus } from "../actions.interfaces";
import { InfoV1 } from "../dto/info.interfaces";
import { GetInfoV1 } from "./info.interfaces";
import { getInfoV1 as getInfoV1REST } from "./info.rest";

export const useGetInfoV1 = (): GetInfoV1 => {
    const [infoV1, setInfoV1] = useState<InfoV1 | null>(null);
    const [status, setStatus] = useState(ActionStatus.Initial);
    const [errorMessage, setErrorMessage] = useState("");

    const getInfoV1 = useCallback(async () => {
        setStatus(ActionStatus.Working);
        try {
            const fetchedInfoV1 = await getInfoV1REST();
            setInfoV1(fetchedInfoV1);
            setStatus(ActionStatus.Done);
            setErrorMessage("");
        } catch (error) {
            const axiosError = error as AxiosError;
            setInfoV1(null);
            setStatus(ActionStatus.Error);
            setErrorMessage(
                axiosError &&
                    axiosError.response &&
                    axiosError.response.data &&
                    axiosError.response.data.message
            );
        }
    }, []);

    return { infoV1, getInfoV1, status, errorMessage };
};
