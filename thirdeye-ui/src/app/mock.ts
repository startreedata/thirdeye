import { Alert } from "./utils/rest/alerts-rest/alerts-rest.interfaces";

export const alerts: Alert[] = [
    {
        updatedBy: "no-auth-user",
        monitoringGranularity: ["1_DAYS"],
        active: true,
        description: "Percentage drop",
        rules: [
            {
                detection: [
                    {
                        name: "detection_rule_1",
                        type: "PERCENTAGE_RULE",
                        params: {
                            offset: "wo1w",
                            percentageChange: 0.1,
                            pattern: "down",
                        },
                    },
                ],
            },
        ],
        filters: null,
        subscriptionGroup: ["sgn"],
        application: ["myApp"],
        createdBy: "admin",
        lastTimestamp: 1590130800000,
        metric: "views",
        name: "pc3",
        datasetNames: ["pageviews"],
        id: 7941,
    },
    {
        updatedBy: "no-auth-user",
        monitoringGranularity: ["1_DAYS"],
        active: true,
        description: "Percentage drop",
        rules: [
            {
                detection: [
                    {
                        name: "detection_rule_1",
                        type: "PERCENTAGE_RULE",
                        params: {
                            offset: "wo1w",
                            percentageChange: 0.1,
                            pattern: "down",
                        },
                    },
                ],
            },
        ],
        filters: null,
        subscriptionGroup: ["sgn"],
        application: ["myApp"],
        createdBy: "admin",
        lastTimestamp: 1590130800000,
        metric: "views",
        name: "pc12",
        datasetNames: ["pageviews"],
        id: 7866,
    },
    {
        updatedBy: "no-auth-user",
        monitoringGranularity: ["1_DAYS"],
        active: true,
        description: "Percentage drop",
        rules: [
            {
                detection: [
                    {
                        name: "detection_rule_1",
                        type: "PERCENTAGE_RULE",
                        params: {
                            offset: "wo1w",
                            percentageChange: 0.1,
                            pattern: "down",
                        },
                    },
                ],
            },
        ],
        filters: null,
        subscriptionGroup: ["sgnDemo"],
        application: ["DemoApp"],
        createdBy: "admin",
        lastTimestamp: 1590130800000,
        metric: "views",
        name: "pcDemo",
        datasetNames: ["pageviews"],
        id: 7605,
    },
    {
        updatedBy: "no-auth-user",
        monitoringGranularity: ["1_DAYS"],
        active: true,
        description: "Percentage drop",
        rules: [
            {
                detection: [
                    {
                        name: "detection_rule_1",
                        type: "PERCENTAGE_RULE",
                        params: {
                            offset: "wo1w",
                            percentageChange: 0.1,
                            pattern: "down",
                        },
                    },
                ],
            },
        ],
        filters: null,
        subscriptionGroup: [],
        application: [],
        createdBy: "admin",
        lastTimestamp: 1590130800000,
        metric: "views",
        name: "pc1",
        datasetNames: ["pageviews"],
        id: 7600,
    },
];
