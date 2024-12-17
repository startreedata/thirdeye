/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { expect, Page } from "@playwright/test";
import { BasePage } from "./base";

export class CreateAlertPage extends BasePage {
    readonly page: Page;
    alertResponseData: any;
    subscriptionResponseData: any;
    anomalyResponseData: any;

    constructor(page: Page) {
        super(page);
        this.page = page;
    }

    async goToCreateAlertPage() {
        await this.page.goto(
            "http://localhost:7004/#access_token=eyJhbGciOiJSUzI1NiIsImtpZCI6Ijk2MjU3ZmNiMzJjNmNkMGU1NDkzN2VmOTg0ODVkY2M0Yzg4NTc5NjgifQ.eyJpc3MiOiJodHRwczovL2lkZW50aXR5LmRlbW8udGhpcmRleWVwcm9kLnN0YXJ0cmVlLmNsb3VkIiwic3ViIjoiQ2lObmIyOW5iR1V0YjJGMWRHZ3lmREV3TWpnMU5UazNNRGM1TlRJeE1EVTJOek0yT0JJa05HRTVNelprTVdVdE1UWmpZaTAwTldVd0xXSTNZMkV0TkRoa016SmxNalF6TldReSIsImF1ZCI6InRoaXJkZXllLXRoaXJkZXllLWRlZmF1bHQiLCJleHAiOjE3MzQ0NTkzMjksImlhdCI6MTczNDM3MjkyOSwibm9uY2UiOiJyYW5kb21fc3RyaW5nIiwiYXRfaGFzaCI6IlRTOWU2bmd6OHluNmwtakJCQlR4TlEiLCJlbWFpbCI6ImhhcnNoaWwuc2hhaEB2ZWxvdGlvLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiSGFyc2hpbCBTaGFoIn0.uCnnBIJR7dBJgp4VC276oCvXlF5YnoAh96SYP83Fqv1dp8HIRGIbodu3IkXalkb3tfDRDujQGCHNTgN4pqagfq5SOze20O0G5HdAj84M0aKoC07pd1qEbX2srVXNKSwzyv8MXdJBhLp2dG5IPq4o-4vn3u9dqeVTV_YjhPaXQ0ERTllSfkOxUWel1AsRnthU7Hfh-ao9GfS6x5lS0PRhb-uXDm0bDV07jCDhedmYlN-JLB0bgYLad4B6tfszXKqEK10xWLgNZKxO7Tsw4k0smvZXTSeuwRYIez_yAcWlIKzSuMDGaGCHYIbldxROjGXsa7B6LMklFoTuCZCttGjCyg"
        );
        await this.page.waitForSelector("h4:has-text('StarTree ThirdEye')", {
            timeout: 10000,
            state: "visible",
        });
        await this.page.goto(
            "http://localhost:7004/alerts/create/new/new-user/easy-alert/"
        );
    }

    async checkHeader() {
        await expect(this.page.locator("h5")).toHaveText("Alert wizard");
    }
}
