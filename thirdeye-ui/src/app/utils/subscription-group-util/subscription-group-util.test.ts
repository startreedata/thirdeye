import i18n from "i18next";
import { cloneDeep } from "lodash";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardData,
} from "../../components/subscription-group-card/subscription-group-card.interfaces";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createEmptySubscriptionGroup,
    createEmptySubscriptionGroupAlert,
    createEmptySubscriptionGroupCardData,
    filterSubscriptionGroups,
    getSubscriptionGroupAlert,
    getSubscriptionGroupAlerts,
    getSubscriptionGroupCardData,
    getSubscriptionGroupCardDatas,
} from "./subscription-group-util";

jest.mock("i18next");

describe("Subscription Group Util", () => {
    beforeAll(() => {
        i18n.t = jest.fn().mockImplementation((key: string): string => {
            return key;
        });
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("createEmptySubscriptionGroup shall create appropriate subscription group", () => {
        const subscriptionGroup = createEmptySubscriptionGroup();

        expect(subscriptionGroup).toEqual(mockEmptySubscriptionGroup);
    });

    test("createEmptySubscriptionGroupCardData shall create appropriate subscription group card data", () => {
        const subscriptionGroupCardData = createEmptySubscriptionGroupCardData();

        expect(subscriptionGroupCardData).toEqual(
            mockEmptySubscriptionGroupCardData
        );
    });

    test("createEmptySubscriptionGroupAlert shall create appropriate subscription group alert", () => {
        const subscriptionGroupAlert = createEmptySubscriptionGroupAlert();

        expect(subscriptionGroupAlert).toEqual(mockEmptySubscriptionGroupAlert);
    });

    test("getSubscriptionGroupCardData shall return empty subscription group card data for invalid subscription group", () => {
        const subscriptionGroupCardData = getSubscriptionGroupCardData(
            (null as unknown) as SubscriptionGroup,
            mockAlerts
        );

        expect(subscriptionGroupCardData).toEqual(
            mockEmptySubscriptionGroupCardData
        );
    });

    test("getSubscriptionGroupCardData shall return appropriate subscription group card data for subscription group when alerts are invalid", () => {
        const expectedSubscriptionGroupCardData = cloneDeep(
            mockSubscriptionGroupCardData1
        );
        expectedSubscriptionGroupCardData.alerts = [];

        const subscriptionGroupCardData = getSubscriptionGroupCardData(
            mockSubscriptionGroup1,
            (null as unknown) as Alert[]
        );

        expect(subscriptionGroupCardData).toEqual(
            expectedSubscriptionGroupCardData
        );
    });

    test("getSubscriptionGroupCardData shall return appropriate subscription group card data for subscription group when alerts are empty", () => {
        const expectedSubscriptionGroupCardData = cloneDeep(
            mockSubscriptionGroupCardData1
        );
        expectedSubscriptionGroupCardData.alerts = [];

        const subscriptionGroupCardData = getSubscriptionGroupCardData(
            mockSubscriptionGroup1,
            []
        );

        expect(subscriptionGroupCardData).toEqual(
            expectedSubscriptionGroupCardData
        );
    });

    test("getSubscriptionGroupCardData shall return appropriate subscription group card data for subscription group and alerts", () => {
        const subscriptionGroupCardData = getSubscriptionGroupCardData(
            mockSubscriptionGroup1,
            mockAlerts
        );

        expect(subscriptionGroupCardData).toEqual(
            mockSubscriptionGroupCardData1
        );
    });

    test("getSubscriptionGroupCardDatas shall return empty array for invalid subscription groups", () => {
        const subscriptionGroupCardDatas = getSubscriptionGroupCardDatas(
            (null as unknown) as SubscriptionGroup[],
            mockAlerts
        );

        expect(subscriptionGroupCardDatas).toEqual([]);
    });

    test("getSubscriptionGroupCardDatas shall return empty array for empty subscription groups", () => {
        const subscriptionGroupCardDatas = getSubscriptionGroupCardDatas(
            [],
            mockAlerts
        );

        expect(subscriptionGroupCardDatas).toEqual([]);
    });

    test("getSubscriptionGroupCardDatas shall return appropriate subscription group card data array for subscription groups when alerts are invalid", () => {
        const expectedSubscriptionGroupCardData1 = cloneDeep(
            mockSubscriptionGroupCardData1
        );
        expectedSubscriptionGroupCardData1.alerts = [];
        const expectedSubscriptionGroupCardData2 = cloneDeep(
            mockSubscriptionGroupCardData2
        );
        expectedSubscriptionGroupCardData2.alerts = [];

        const subscriptionGroupCardDatas = getSubscriptionGroupCardDatas(
            mockSubscriptionGroups,
            (null as unknown) as Alert[]
        );

        expect(subscriptionGroupCardDatas).toEqual([
            expectedSubscriptionGroupCardData1,
            expectedSubscriptionGroupCardData2,
        ]);
    });

    test("getSubscriptionGroupCardDatas shall return appropriate subscription group card data array for subscription groups when alerts are empty", () => {
        const expectedSubscriptionGroupCardData1 = cloneDeep(
            mockSubscriptionGroupCardData1
        );
        expectedSubscriptionGroupCardData1.alerts = [];
        const expectedSubscriptionGroupCardData2 = cloneDeep(
            mockSubscriptionGroupCardData2
        );
        expectedSubscriptionGroupCardData2.alerts = [];

        const subscriptionGroupCardDatas = getSubscriptionGroupCardDatas(
            mockSubscriptionGroups,
            []
        );

        expect(subscriptionGroupCardDatas).toEqual([
            expectedSubscriptionGroupCardData1,
            expectedSubscriptionGroupCardData2,
        ]);
    });

    test("getSubscriptionGroupCardDatas shall return appropriate subscription group card data array for subscription groups and alerts", () => {
        const subscriptionGroupCardDatas = getSubscriptionGroupCardDatas(
            mockSubscriptionGroups,
            mockAlerts
        );

        expect(subscriptionGroupCardDatas).toEqual(subscriptionGroupCardDatas);
    });

    test("getSubscriptionGroupAlert shall return empty subscription group alert for invalid alert", () => {
        const subscriptionGroupAlert = getSubscriptionGroupAlert(
            (null as unknown) as Alert
        );

        expect(subscriptionGroupAlert).toEqual(mockEmptySubscriptionGroupAlert);
    });

    test("getSubscriptionGroupAlert shall return appropriate subscription group alert for alert", () => {
        const subscriptionGroupAlert = getSubscriptionGroupAlert(mockAlert1);

        expect(subscriptionGroupAlert).toEqual(mockSubscriptionGroupAlert1);
    });

    test("getSubscriptionGroupAlert shall return empty array for invalid alerts", () => {
        const subscriptionGroupAlerts = getSubscriptionGroupAlerts(
            (null as unknown) as Alert[]
        );

        expect(subscriptionGroupAlerts).toEqual([]);
    });

    test("getSubscriptionGroupAlert shall return empty array for empty alerts", () => {
        const subscriptionGroupAlerts = getSubscriptionGroupAlerts([]);

        expect(subscriptionGroupAlerts).toEqual([]);
    });

    test("getSubscriptionGroupAlert shall return appropriate subscription group alert array for alerts", () => {
        const subscriptionGroupAlerts = getSubscriptionGroupAlerts(mockAlerts);

        expect(subscriptionGroupAlerts).toEqual(mockSubscriptionGroupAlerts);
    });

    test("filterSubscriptionGroups shall return empty array for invalid subscription group card data array", () => {
        const subscriptionGroups = filterSubscriptionGroups(
            (null as unknown) as SubscriptionGroupCardData[],
            mockSearchWords
        );

        expect(subscriptionGroups).toEqual([]);
    });

    test("filterSubscriptionGroups shall return empty array for empty subscription group card data array", () => {
        const subscriptionGroups = filterSubscriptionGroups(
            [],
            mockSearchWords
        );

        expect(subscriptionGroups).toEqual([]);
    });

    test("filterSubscriptionGroups shall return appropriate subscription group card data array for subscription group card data array when search words are invalid", () => {
        const subscriptionGroups = filterSubscriptionGroups(
            mockSubscriptionGroupCardDatas,
            (null as unknown) as string[]
        );

        expect(subscriptionGroups).toEqual(mockSubscriptionGroupCardDatas);
    });

    test("filterSubscriptionGroups shall return appropriate subscription group card data array for subscription group card data array when search words are empty", () => {
        const subscriptionGroups = filterSubscriptionGroups(
            mockSubscriptionGroupCardDatas,
            []
        );

        expect(subscriptionGroups).toEqual(mockSubscriptionGroupCardDatas);
    });

    test("filterSubscriptionGroups shall return appropriate subscription group card data array for subscription group card data array and search words", () => {
        const subscriptionGroups = filterSubscriptionGroups(
            mockSubscriptionGroupCardDatas,
            mockSearchWords
        );

        expect(subscriptionGroups).toHaveLength(1);
        expect(subscriptionGroups[0]).toEqual(mockSubscriptionGroupCardData1);
    });
});

const mockEmptySubscriptionGroup: SubscriptionGroup = ({
    name: "",
    alerts: [],
    emailSettings: {
        to: [],
    },
} as unknown) as SubscriptionGroup;

const mockEmptySubscriptionGroupCardData: SubscriptionGroupCardData = {
    id: -1,
    name: "label.no-data-available-marker",
    alerts: [],
    emails: [],
    subscriptionGroup: null,
};

const mockEmptySubscriptionGroupAlert: SubscriptionGroupAlert = {
    id: -1,
    name: "label.no-data-available-marker",
};

const mockSubscriptionGroup1: SubscriptionGroup = {
    id: 1,
    name: "testSubscriptionGroupName1",
    alerts: [
        {
            id: 2,
        },
        {
            id: 3,
        },
        {
            id: 4,
        },
    ],
    emailSettings: {
        to: ["testEmail1", "testEmail2"],
    },
} as SubscriptionGroup;

const mockSubscriptionGroup2: SubscriptionGroup = {
    id: 5,
    name: "testSubscriptionGroupName5",
    alerts: [] as Alert[],
    emailSettings: {},
} as SubscriptionGroup;

const mockSubscriptionGroups = [mockSubscriptionGroup1, mockSubscriptionGroup2];

const mockAlert1: Alert = {
    id: 2,
    name: "testAlertName2",
} as Alert;

const mockAlert2: Alert = {
    id: 3,
    name: "testAlertName3",
} as Alert;

const mockAlert3: Alert = {
    id: 6,
    name: "testAlertName6",
} as Alert;

const mockAlerts = [mockAlert1, mockAlert2, mockAlert3];

const mockSubscriptionGroupAlert1: SubscriptionGroupAlert = {
    id: 2,
    name: "testAlertName2",
};

const mockSubscriptionGroupAlert2: SubscriptionGroupAlert = {
    id: 3,
    name: "testAlertName3",
};

const mockSubscriptionGroupAlert3: SubscriptionGroupAlert = {
    id: 6,
    name: "testAlertName6",
};

const mockSubscriptionGroupAlerts = [
    mockSubscriptionGroupAlert1,
    mockSubscriptionGroupAlert2,
    mockSubscriptionGroupAlert3,
];

const mockSubscriptionGroupCardData1: SubscriptionGroupCardData = {
    id: 1,
    name: "testSubscriptionGroupName1",
    alerts: [
        {
            id: 2,
            name: "testAlertName2",
        },
        {
            id: 3,
            name: "testAlertName3",
        },
    ],
    emails: ["testEmail1", "testEmail2"],
    subscriptionGroup: mockSubscriptionGroup1,
};

const mockSubscriptionGroupCardData2: SubscriptionGroupCardData = {
    id: 5,
    name: "testSubscriptionGroupName5",
    alerts: [],
    emails: [],
    subscriptionGroup: mockSubscriptionGroup2,
};

const mockSubscriptionGroupCardDatas = [
    mockSubscriptionGroupCardData1,
    mockSubscriptionGroupCardData2,
];

const mockSearchWords = ["name1", "name2"];
