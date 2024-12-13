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
import { Page } from "@playwright/test";

export class BasePage {
    readonly page: Page;

    constructor(page: Page) {
        this.page = page;
    }

    async gotoHomePage() {
        await this.page.goto(
            "http://localhost:7004/#access_token=eyJhbGciOiJSUzI1NiIsImtpZCI6Ijk2MjU3ZmNiMzJjNmNkMGU1NDkzN2VmOTg0ODVkY2M0Yzg4NTc5NjgifQ.eyJpc3MiOiJodHRwczovL2lkZW50aXR5LmRlbW8udGhpcmRleWVwcm9kLnN0YXJ0cmVlLmNsb3VkIiwic3ViIjoiQ2lObmIyOW5iR1V0YjJGMWRHZ3lmREV3T1RVMk5qZ3dOekkwTVRrek5UVTRNakUxTnhJa05HRTVNelprTVdVdE1UWmpZaTAwTldVd0xXSTNZMkV0TkRoa016SmxNalF6TldReSIsImF1ZCI6InRoaXJkZXllLXRoaXJkZXllLWRlZmF1bHQiLCJleHAiOjE3MzQxMDIyMDgsImlhdCI6MTczNDAxNTgwOCwibm9uY2UiOiJyYW5kb21fc3RyaW5nIiwiYXRfaGFzaCI6InphanZqTjZpQW9Oc2UyUW9IeDQwV1EiLCJlbWFpbCI6Im5hbGluQHN0YXJ0cmVlLmFpIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImdyb3VwcyI6WyJzdGFydHJlZS1vcHMtZGVmYXVsdC1zcmUiXSwibmFtZSI6Ik5hbGluIFBhdGlkYXIifQ.g5bYJAcv8SEXb9G6pUQVex4W5Lw_MoT2KUf5MldwxnlE5vyjifvSyuiDxrzRAhtXY1PIyAOeuGXtj8hWqQJTuEDq41mgy9AQsKpVIrsXgcb86VGSN0Ouj5bIpn9Uj9xiRvjCbBpJdImei1iL52JR4p_VJLpzj-fPfwPqxgOqsdElpTTYLkzguH1T_GKXd2OcnbfYzkpnh_sTT_sZbI7MGA0Haakzh8ziczJL0m4y7yKT7WiuQmXsoAkWIlAVuTHgENbiJSYxOPe1bOcgAUtEKSfnZ0qFr0ERf-pIIwaqKR-bhUwLDDDWyBunL2XAytmY4S9Z0L1ecWzh3-onOaCvzQ"
        );
        await this.page.waitForURL("http://localhost:7004/home");
    }
}
