export interface AlertCronForm {
    minute: string;
    hour: string;
    day: string;
    month: string;
    week: string;
}

export interface AlertCronCalculatorProps {
    handleCronInputChange: (value: string) => void;
    value?: string;
}
