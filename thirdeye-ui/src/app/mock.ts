/* eslint-disable max-len */
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

export const DETECTION_CONFIG =
    "---\n- id: 0\n  name: string\n  description: string\n  cron: string\n  lastTimestamp: '2020-11-05T10:57:37.404Z'\n  active: true\n  created: '2020-11-05T10:57:37.404Z'\n  updated: '2020-11-05T10:57:37.404Z'\n  owner:\n    id: 0\n    principal: string\n    created: '2020-11-05T10:57:37.404Z'\n  detections:\n    additionalProp1:\n      name: string\n      type: string\n      metric:\n        id: 0\n        name: string\n        urn: string\n        dataset:\n          id: 0\n          name: string\n          active: true\n          additive: true\n          dimensions:\n          - string\n          timeColumn:\n            name: string\n            interval:\n              seconds: 0\n              nano: 0\n              negative: true\n              zero: true\n              units:\n              - dateBased: true\n                timeBased: true\n                durationEstimated: true\n            format: string\n            timezone: string\n          expectedDelay:\n            seconds: 0\n            nano: 0\n            negative: true\n            zero: true\n            units:\n            - dateBased: true\n              timeBased: true\n              durationEstimated: true\n        active: true\n        created: '2020-11-05T10:57:37.404Z'\n        updated: '2020-11-05T10:57:37.404Z'\n      params:\n        additionalProp1: {}\n        additionalProp2: {}\n        additionalProp3: {}\n    additionalProp2:\n      name: string\n      type: string\n      metric:\n        id: 0\n        name: string\n        urn: string\n        dataset:\n          id: 0\n          name: string\n          active: true\n          additive: true\n          dimensions:\n          - string\n          timeColumn:\n            name: string\n            interval:\n              seconds: 0\n              nano: 0\n              negative: true\n              zero: true\n              units:\n              - dateBased: true\n                timeBased: true\n                durationEstimated: true\n            format: string\n            timezone: string\n          expectedDelay:\n            seconds: 0\n            nano: 0\n            negative: true\n            zero: true\n            units:\n            - dateBased: true\n              timeBased: true\n              durationEstimated: true\n        active: true\n        created: '2020-11-05T10:57:37.404Z'\n        updated: '2020-11-05T10:57:37.404Z'\n      params:\n        additionalProp1: {}\n        additionalProp2: {}\n        additionalProp3: {}\n    additionalProp3:\n      name: string\n      type: string\n      metric:\n        id: 0\n        name: string\n        urn: string\n        dataset:\n          id: 0\n          name: string\n          active: true\n          additive: true\n          dimensions:\n          - string\n          timeColumn:\n            name: string\n            interval:\n              seconds: 0\n              nano: 0\n              negative: true\n              zero: true\n              units:\n              - dateBased: true\n                timeBased: true\n                durationEstimated: true\n            format: string\n            timezone: string\n          expectedDelay:\n            seconds: 0\n            nano: 0\n            negative: true\n            zero: true\n            units:\n            - dateBased: true\n              timeBased: true\n              durationEstimated: true\n        active: true\n        created: '2020-11-05T10:57:37.404Z'\n        updated: '2020-11-05T10:57:37.404Z'\n      params:\n        additionalProp1: {}\n        additionalProp2: {}\n        additionalProp3: {}\n  filters:\n    additionalProp1:\n      name: string\n      type: string\n      metric:\n        id: 0\n        name: string\n        urn: string\n        dataset:\n          id: 0\n          name: string\n          active: true\n          additive: true\n          dimensions:\n          - string\n          timeColumn:\n            name: string\n            interval:\n              seconds: 0\n              nano: 0\n              negative: true\n              zero: true\n              units:\n              - dateBased: true\n                timeBased: true\n                durationEstimated: true\n            format: string\n            timezone: string\n          expectedDelay:\n            seconds: 0\n            nano: 0\n            negative: true\n            zero: true\n            units:\n            - dateBased: true\n              timeBased: true\n              durationEstimated: true\n        active: true\n        created: '2020-11-05T10:57:37.405Z'\n        updated: '2020-11-05T10:57:37.405Z'\n      params:\n        additionalProp1: {}\n        additionalProp2: {}\n        additionalProp3: {}\n    additionalProp2:\n      name: string\n      type: string\n      metric:\n        id: 0\n        name: string\n        urn: string\n        dataset:\n          id: 0\n          name: string\n          active: true\n          additive: true\n          dimensions:\n          - string\n          timeColumn:\n            name: string\n            interval:\n              seconds: 0\n              nano: 0\n              negative: true\n              zero: true\n              units:\n              - dateBased: true\n                timeBased: true\n                durationEstimated: true\n            format: string\n            timezone: string\n          expectedDelay:\n            seconds: 0\n            nano: 0\n            negative: true\n            zero: true\n            units:\n            - dateBased: true\n              timeBased: true\n              durationEstimated: true\n        active: true\n        created: '2020-11-05T10:57:37.405Z'\n        updated: '2020-11-05T10:57:37.405Z'\n      params:\n        additionalProp1: {}\n        additionalProp2: {}\n        additionalProp3: {}\n    additionalProp3:\n      name: string\n      type: string\n      metric:\n        id: 0\n        name: string\n        urn: string\n        dataset:\n          id: 0\n          name: string\n          active: true\n          additive: true\n          dimensions:\n          - string\n          timeColumn:\n            name: string\n            interval:\n              seconds: 0\n              nano: 0\n              negative: true\n              zero: true\n              units:\n              - dateBased: true\n                timeBased: true\n                durationEstimated: true\n            format: string\n            timezone: string\n          expectedDelay:\n            seconds: 0\n            nano: 0\n            negative: true\n            zero: true\n            units:\n            - dateBased: true\n              timeBased: true\n              durationEstimated: true\n        active: true\n        created: '2020-11-05T10:57:37.405Z'\n        updated: '2020-11-05T10:57:37.405Z'\n      params:\n        additionalProp1: {}\n        additionalProp2: {}\n        additionalProp3: {}\n  qualityChecks:\n    additionalProp1:\n      name: string\n      type: string\n      metric:\n        id: 0\n        name: string\n        urn: string\n        dataset:\n          id: 0\n          name: string\n          active: true\n          additive: true\n          dimensions:\n          - string\n          timeColumn:\n            name: string\n            interval:\n              seconds: 0\n              nano: 0\n              negative: true\n              zero: true\n              units:\n              - dateBased: true\n                timeBased: true\n                durationEstimated: true\n            format: string\n            timezone: string\n          expectedDelay:\n            seconds: 0\n            nano: 0\n            negative: true\n            zero: true\n            units:\n            - dateBased: true\n              timeBased: true\n              durationEstimated: true\n        active: true\n        created: '2020-11-05T10:57:37.405Z'\n        updated: '2020-11-05T10:57:37.405Z'\n      params:\n        additionalProp1: {}\n        additionalProp2: {}\n        additionalProp3: {}\n    additionalProp2:\n      name: string\n      type: string\n      metric:\n        id: 0\n        name: string\n        urn: string\n        dataset:\n          id: 0\n          name: string\n          active: true\n          additive: true\n          dimensions:\n          - string\n          timeColumn:\n            name: string\n            interval:\n              seconds: 0\n              nano: 0\n              negative: true\n              zero: true\n              units:\n              - dateBased: true\n                timeBased: true\n                durationEstimated: true\n            format: string\n            timezone: string\n          expectedDelay:\n            seconds: 0\n            nano: 0\n            negative: true\n            zero: true\n            units:\n            - dateBased: true\n              timeBased: true\n              durationEstimated: true\n        active: true\n        created: '2020-11-05T10:57:37.405Z'\n        updated: '2020-11-05T10:57:37.405Z'\n      params:\n        additionalProp1: {}\n        additionalProp2: {}\n        additionalProp3: {}\n    additionalProp3:\n      name: string\n      type: string\n      metric:\n        id: 0\n        name: string\n        urn: string\n        dataset:\n          id: 0\n          name: string\n          active: true\n          additive: true\n          dimensions:\n          - string\n          timeColumn:\n            name: string\n            interval:\n              seconds: 0\n              nano: 0\n              negative: true\n              zero: true\n              units:\n              - dateBased: true\n                timeBased: true\n                durationEstimated: true\n            format: string\n            timezone: string\n          expectedDelay:\n            seconds: 0\n            nano: 0\n            negative: true\n            zero: true\n            units:\n            - dateBased: true\n              timeBased: true\n              durationEstimated: true\n        active: true\n        created: '2020-11-05T10:57:37.405Z'\n        updated: '2020-11-05T10:57:37.405Z'\n      params:\n        additionalProp1: {}\n        additionalProp2: {}\n        additionalProp3: {}\n";
