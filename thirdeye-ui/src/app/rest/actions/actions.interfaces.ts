export interface ActionHook {
    status: ActionStatus;
    errorMessage: string;
}

export enum ActionStatus {
    INITIAL = "INITIAL",
    FETCHING = "FETCHING",
    DONE = "DONE",
    ERROR = "ERROR",
}
