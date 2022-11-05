
import {
    SpecType,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import { validateSubscriptionGroup } from "./subscription-group-whizard.utils";

describe("Subscription Groups Whizard Util", () => {
    it("validateSubscriptionGroup return true if name and cron are valid", () => {
        expect(
            validateSubscriptionGroup({
                ...VALID_BASIC_INPUT,
            } as SubscriptionGroup)
        ).toEqual(true);
    });

    it("validateSubscriptionGroup return false if name valid and cron is invalid", () => {
        expect(
            validateSubscriptionGroup({
                name: "hello world",
                cron: "0 */",
            } as SubscriptionGroup)
        ).toEqual(false);
    });

    it("validateSubscriptionGroup return true if specs are valid", () => {
        expect(
            validateSubscriptionGroup({
                ...VALID_BASIC_INPUT,
                specs: [
                    {
                        type: SpecType.EmailSendgrid,
                        params: {
                            apiKey: "hellowowlrd",
                            emailRecipients: {
                                from: "aname@startree.ai",
                            },
                        },
                    },
                    {
                        type: SpecType.Slack,
                        params: {
                            webhookUrl:
                                "https://hooks.slack.com/services/T0/B0/XXXXX",
                        },
                    },
                    {
                        type: SpecType.Webhook,
                        params: {
                            url: "https://hooks.slack.com/services/T0/B0/XXXXX",
                        },
                    },
                ],
            } as SubscriptionGroup)
        ).toEqual(true);
    });

    it("validateSubscriptionGroup return false if specs are invalid", () => {
        expect(
            validateSubscriptionGroup({
                ...VALID_BASIC_INPUT,
                specs: [
                    {
                        type: SpecType.EmailSendgrid,
                        params: {
                            apiKey: "",
                            emailRecipients: {
                                from: "",
                            },
                        },
                    },
                    {
                        type: SpecType.Slack,
                        params: {
                            webhookUrl: "",
                        },
                    },
                    {
                        type: SpecType.Webhook,
                        params: {
                            url: "",
                        },
                    },
                ],
            } as SubscriptionGroup)
        ).toEqual(false);
    });
});

const VALID_BASIC_INPUT = {
    name: "hello world",
    cron: "0 */5 * * * ?",
};
