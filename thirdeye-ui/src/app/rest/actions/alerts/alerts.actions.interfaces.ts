import { Alert, AlertEvaluation } from "../../dto/alert.interfaces";
import { ActionHook } from "../actions.interfaces";

export interface FetchAlert extends ActionHook {
    alert: Alert | null;
    fetchAlert: (id: number) => Promise<void>;
}

export interface FetchAllAlerts extends ActionHook {
    alerts: Alert[] | null;
    fetchAllAlerts: () => Promise<void>;
}

export interface CreateAlert extends ActionHook {
    createAlert: (alert: Alert) => Promise<void>;
}

export interface CreateAlerts extends ActionHook {
    createAlerts: (alerts: Alert[]) => Promise<void>;
}

export interface UpdateAlert extends ActionHook {
    updateAlert: (alert: Alert) => Promise<void>;
}

export interface UpdateAlerts extends ActionHook {
    updateAlerts: (alerts: Alert[]) => Promise<void>;
}

export interface DeleteAlert extends ActionHook {
    deleteAlert: (id: number) => Promise<void>;
}

export interface FetchAlertEvaluation extends ActionHook {
    alertEvaluation: AlertEvaluation | null;
    fetchAlertEvaluation: (alertEvaluation: AlertEvaluation) => Promise<void>;
}
