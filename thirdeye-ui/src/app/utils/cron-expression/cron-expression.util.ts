import { QuickScheduleOption } from "../../components/cron-editor/cron-editor.interfaces";

export const QuickScheduleOptions: Array<QuickScheduleOption> = [
    {
        label: "Hourly",
        key: "hourly",
        value: "0 * * * *",
    },
    {
        label: "Monthly",
        key: "monthly",
        value: "0 0 1 * *",
    },
    {
        label: "Weekly",
        key: "weekly",
        value: "0 0 * * 0",
    },
    {
        label: "Yearly",
        key: "yearly",
        value: "0 0 1 1 *",
    },
    {
        label: "Daily",
        key: "daily",
        value: "0 0 * * *",
    },
];
