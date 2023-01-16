<#--

    Copyright 2023 StarTree Inc

    Licensed under the StarTree Community License (the "License"); you may not use
    this file except in compliance with the License. You may obtain a copy of the
    License at http://www.startree.ai/legal/startree-community-license

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
    either express or implied.
    See the License for the specific language governing permissions and limitations under
    the License.

-->
<#macro addBlock title align>
    <tr>
      <td style="border-bottom: 1px solid rgba(0,0,0,0.15); padding: 12px 24px; align:${align}">
        <#if title?has_content>
          <p style="font-size:20px; line-height:24px; color:#1D1D1D; font-weight: 500; margin:0; padding:0;">${title}</p>
        </#if>

        <#nested>

      </td>
    </tr>
</#macro>