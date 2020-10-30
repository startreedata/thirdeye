import { AlertData } from './components/alerts/alert-card.component';

export const alerts: AlertData[] = [
    {
        name: "Alert Name Active Normal example",
        metric: "Kafka",
        dataset: "MyDatasetOne",
        application: "Test",
        createdBy: "sample.owner",
        filteredBy: "",
        breakdownBy: "",
        detectionType: "PERCENTAGE_RULE",
        subscriptionGroup: "sgn1"
    },
    {
        name: "Alert Name Active Poor example",
        metric: "Kafka",
        dataset: "Application",
        application: "",
        createdBy: "sample.owner",
        filteredBy: "",
        breakdownBy: "",
        detectionType: "PERCENTAGE_RULE",
        subscriptionGroup: "sgn1"
    }
]