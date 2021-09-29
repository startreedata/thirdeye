import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import { SubscriptionGroup } from "../../dto/subscription-group.interfaces";
import {
    createSubscriptionGroup,
    createSubscriptionGroups,
    deleteSubscriptionGroup,
    getAllSubscriptionGroups,
    getSubscriptionGroup,
    updateSubscriptionGroup,
    updateSubscriptionGroups,
} from "../../subscription-groups/subscription-groups.rest";
import { ActionStatus } from "../actions.interfaces";
import {
    CreateSubscriptionGroup,
    CreateSubscriptionGroups,
    DeleteSubscriptionGroup,
    FetchAllSubscriptionGroups,
    FetchSubscriptionGroup,
    UpdateSubscriptionGroup,
    UpdateSubscriptionGroups,
} from "./subscription-group.actions.interfaces";

export const useFetchSubscriptionGroup = (): FetchSubscriptionGroup => {
    const [
        subscriptionGroup,
        setSubscriptionGroup,
    ] = useState<SubscriptionGroup | null>(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchSubscriptionGroup = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            const subscriptionGroup = await getSubscriptionGroup(id);

            setSubscriptionGroup(subscriptionGroup);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setSubscriptionGroup(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return { subscriptionGroup, errorMessage, status, fetchSubscriptionGroup };
};

export const useFetchAllSubscriptionGroups = (): FetchAllSubscriptionGroups => {
    const [subscriptionGroups, setSubscriptionGroup] = useState<
        SubscriptionGroup[] | null
    >(null);
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const fetchAllSubscriptionGroups = useCallback(async () => {
        setStatus(ActionStatus.FETCHING);
        try {
            const subscriptionGroups = await getAllSubscriptionGroups();

            setSubscriptionGroup(subscriptionGroups);
            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setSubscriptionGroup(null);
            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return {
        subscriptionGroups,
        errorMessage,
        status,
        fetchAllSubscriptionGroups,
    };
};

export const useCreateSubscriptionGroup = (): CreateSubscriptionGroup => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchCreateSubscriptionGroup = useCallback(
        async (subscriptionGroup: SubscriptionGroup) => {
            setStatus(ActionStatus.FETCHING);
            try {
                await createSubscriptionGroup(subscriptionGroup);

                setStatus(ActionStatus.DONE);
                setErrorMessage("");
            } catch (error) {
                const errorMessage = (error as AxiosError).response?.data
                    .message;

                setStatus(ActionStatus.ERROR);
                setErrorMessage(errorMessage);
            }
        },
        []
    );

    return {
        errorMessage,
        status,
        createSubscriptionGroup: dispatchCreateSubscriptionGroup,
    };
};

export const useCreateSubscriptionGroups = (): CreateSubscriptionGroups => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchCreateSubscriptionGroups = useCallback(
        async (subscriptionGroups: SubscriptionGroup[]) => {
            setStatus(ActionStatus.FETCHING);
            try {
                await createSubscriptionGroups(subscriptionGroups);

                setStatus(ActionStatus.DONE);
                setErrorMessage("");
            } catch (error) {
                const errorMessage = (error as AxiosError).response?.data
                    .message;

                setStatus(ActionStatus.ERROR);
                setErrorMessage(errorMessage);
            }
        },
        []
    );

    return {
        errorMessage,
        status,
        createSubscriptionGroups: dispatchCreateSubscriptionGroups,
    };
};

export const useUpdateSubscriptionGroup = (): UpdateSubscriptionGroup => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchUpdateSubscriptionGroup = useCallback(
        async (subscriptionGroup: SubscriptionGroup) => {
            setStatus(ActionStatus.FETCHING);
            try {
                await updateSubscriptionGroup(subscriptionGroup);

                setStatus(ActionStatus.DONE);
                setErrorMessage("");
            } catch (error) {
                const errorMessage = (error as AxiosError).response?.data
                    .message;

                setStatus(ActionStatus.ERROR);
                setErrorMessage(errorMessage);
            }
        },
        []
    );

    return {
        errorMessage,
        status,
        updateSubscriptionGroup: dispatchUpdateSubscriptionGroup,
    };
};

export const useUpdateSubscriptionGroups = (): UpdateSubscriptionGroups => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchUpdateSubscriptionGroups = useCallback(
        async (subscriptionGroups: SubscriptionGroup[]) => {
            setStatus(ActionStatus.FETCHING);
            try {
                await updateSubscriptionGroups(subscriptionGroups);

                setStatus(ActionStatus.DONE);
                setErrorMessage("");
            } catch (error) {
                const errorMessage = (error as AxiosError).response?.data
                    .message;

                setStatus(ActionStatus.ERROR);
                setErrorMessage(errorMessage);
            }
        },
        []
    );

    return {
        errorMessage,
        status,
        updateSubscriptionGroups: dispatchUpdateSubscriptionGroups,
    };
};

export const useDeleteSubscriptionGroup = (): DeleteSubscriptionGroup => {
    const [errorMessage, setErrorMessage] = useState("");
    const [status, setStatus] = useState(ActionStatus.INITIAL);

    const dispatchDeleteSubscriptionGroup = useCallback(async (id: number) => {
        setStatus(ActionStatus.FETCHING);
        try {
            await deleteSubscriptionGroup(id);

            setStatus(ActionStatus.DONE);
            setErrorMessage("");
        } catch (error) {
            const errorMessage = (error as AxiosError).response?.data.message;

            setStatus(ActionStatus.ERROR);
            setErrorMessage(errorMessage);
        }
    }, []);

    return {
        errorMessage,
        status,
        deleteSubscriptionGroup: dispatchDeleteSubscriptionGroup,
    };
};
