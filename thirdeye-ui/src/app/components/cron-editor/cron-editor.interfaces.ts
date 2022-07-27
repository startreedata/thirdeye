export interface CronEditorProps {
    value: string;
    onChange: (cron: string) => void;
    hideQuickOptions?: boolean;
    label?: string;
}

export type QuickScheduleOptionKeys =
    | "hourly"
    | "monthly"
    | "daily"
    | "yearly"
    | "weekly";

export type QuickScheduleOption = {
    key: QuickScheduleOptionKeys;
    value: string;
    label: string;
};
