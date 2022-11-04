export enum ActionStatus {
    Initial,
    Working,
    Done,
    Error,
}

export interface ActionHook {
    status: ActionStatus;
    errorMessage: string;
}
