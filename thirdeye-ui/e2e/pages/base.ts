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
        // await this.page.goto(
        //     "http://localhost:7004/#access_token=eyJhbGciOiJSUzI1NiIsImtpZCI6Ijk2MjU3ZmNiMzJjNmNkMGU1NDkzN2VmOTg0ODVkY2M0Yzg4NTc5NjgifQ.eyJpc3MiOiJodHRwczovL2lkZW50aXR5LmRlbW8udGhpcmRleWVwcm9kLnN0YXJ0cmVlLmNsb3VkIiwic3ViIjoiQ2lObmIyOW5iR1V0YjJGMWRHZ3lmREV3T1RVMk5qZ3dOekkwTVRrek5UVTRNakUxTnhJa05HRTVNelprTVdVdE1UWmpZaTAwTldVd0xXSTNZMkV0TkRoa016SmxNalF6TldReSIsImF1ZCI6InRoaXJkZXllLXRoaXJkZXllLWRlZmF1bHQiLCJleHAiOjE3MzQ0Mjc4MDgsImlhdCI6MTczNDM0MTQwOCwibm9uY2UiOiJyYW5kb21fc3RyaW5nIiwiYXRfaGFzaCI6Il85cUVLYWliZ3RGT0M2WGdFc0VxOHciLCJlbWFpbCI6Im5hbGluQHN0YXJ0cmVlLmFpIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImdyb3VwcyI6WyJzdGFydHJlZS1vcHMtZGVmYXVsdC1zcmUiXSwibmFtZSI6Ik5hbGluIFBhdGlkYXIifQ.k2TM_loNTgXJrryaZwaxp-439dTtt7-bE0bqpmUp-WbliI4gFZ0JGMeUWzv3ca7jCl1lTEDiU_fEwLgLVaMXGFUQMbGvyZEKQXfdkNCGZHnTiPdfk2QVM4XrgyW_H5nn1mvnv9X1ou7Vv3dTXKyYTO_ZnYVzTJBJIY3A3tKuQNmFRJs9fPZEaOTCgRpaH_7EwH2Lpebj9wHOeshxVBW6RXQDvXi0ehuU6cY0CRBSZLKeN_-0YYGr3cZr-HSNlvU7ZWZxqWGYxfg2Ro3rFzXuyPYebR6uNeVn5dWQC7s3c1GvkdnMjxXRmnXpdVAIefIi2P3Z89HcPEp71Ukcleu8-g"
        // );
        await this.page.goto("http://localhost:7004");
        await this.page.waitForURL("http://localhost:7004/home");
    }
}

// VERCEL_ACCESS_TOKEN="eyJhbGciOiJSUzI1NiIsImtpZCI6Ijk2MjU3ZmNiMzJjNmNkMGU1NDkzN2VmOTg0ODVkY2M0Yzg4NTc5NjgifQ.eyJpc3MiOiJodHRwczovL2lkZW50aXR5LmRlbW8udGhpcmRleWVwcm9kLnN0YXJ0cmVlLmNsb3VkIiwic3ViIjoiQ2lObmIyOW5iR1V0YjJGMWRHZ3lmREV3T1RVMk5qZ3dOekkwTVRrek5UVTRNakUxTnhJa05HRTVNelprTVdVdE1UWmpZaTAwTldVd0xXSTNZMkV0TkRoa016SmxNalF6TldReSIsImF1ZCI6InRoaXJkZXllLXRoaXJkZXllLWRlZmF1bHQiLCJleHAiOjE3MzQ0NTI2MzQsImlhdCI6MTczNDM2NjIzNCwibm9uY2UiOiJyYW5kb21fc3RyaW5nIiwiYXRfaGFzaCI6InEtY0M2SlRZeW5yR1ZZdzA0RXdUcWciLCJlbWFpbCI6Im5hbGluQHN0YXJ0cmVlLmFpIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImdyb3VwcyI6WyJzdGFydHJlZS1vcHMtZGVmYXVsdC1zcmUiXSwibmFtZSI6Ik5hbGluIFBhdGlkYXIifQ.L2XtxIITBBhDfYwesK2libs5y68dFPNsVROUHMhni6BpvGd2u-cQ8ZiKWDJIGIJBlJiHIu2fG03jiaJzC8wcnQ_Jk2U2O8QOW7VRFiaDFwz0SMh5UtVrxZ-Y5Z7kVrRNdy5rWxeChlBzZMjVjVfPowWlfR5IAVsKygREjndExGje5NlCfyLQDeN6L_Xs2Hc0infw0Lw0Ez0GGqQ6pu_bFYMadFCWnA1ZpPK9JoscSMBbgrANMHlQxM7LsY9MjQypWelwPDgYlCM0VcZhROcESVlwR6K-YztN5B5AbvzzbVpVt5C-OkEUA_1DyU9enWw3is51jcHOe-hGPki-P0RF7A" npm run start
