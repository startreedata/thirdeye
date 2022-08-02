<#--

    Copyright 2022 StarTree Inc

    Licensed under the StarTree Community License (the "License"); you may not use
    this file except in compliance with the License. You may obtain a copy of the
    License at http://www.startree.ai/legal/startree-community-license

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
    either express or implied.
    See the License for the specific language governing permissions and limitations under
    the License.

-->
<head>
  <link href="https://fonts.googleapis.com/css?family=Open+Sans" rel="stylesheet">
</head>

<body style="background-color: #EDF0F3;">
  <table border="0" cellpadding="0" cellspacing="0" width="100%" style="width:100%; font-family: 'Proxima Nova','Arial', 'Helvetica Neue',Helvetica, sans-serif;font-size:16px;line-height:normal;margin:0 auto; max-width: 700px; background-color: #F3F6F8; margin: 0 auto;">
    <tr style="background-color: #F3F6F8;">
      <td align="left" style="padding: 12px 24px; height: 60px; background-color: #F6F8FA;" colspan="2">
        <img width="35" height="35" alt="logo" src="https://static.licdn-ei.com/scds/common/u/images/email/logos/logo_shift_inbug_82x82_v1.png"
          style="vertical-align: middle; display: inline-block; margin-right: 8px; background: white;">
        <span style="color: rgba(0,0,0,0.75);font-size: 18px; font-weight: bold; letter-spacing: 2px; display: inline-block;vertical-align: middle;">THIRDEYE ALERT</span>
      </td>
    </tr>

    <tr>
      <td>
        <table border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color:white; border:1px solid #E9E9E9; border-radius: 2px; width: 100%;">
          <tr>
            <td style="padding: 0px 80px 24px 80px; background-color: #FFF; color: #FFF; text-align: center" colspan="2">
              <p style="font-size: 20px; font-weight: 500; margin-bottom: 8px; color: #1D1D1D">You will now  receive alerts from ${functionName}</p>
              <p style="font-size: 16px; font-weight: 300; 	color: #1D1D1D;">This alert would have detected ${anomalyCount} anomalies in the past ${repalyDays} days.</p>
            </td>
          </tr>
          <tr>
            <td style="padding: 24px 80px; background-color: #FFF;">
              <table border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color:white; width: 100%;">

              <tr>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px;">
                  Metric
                </td>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px; font-weight: bold;">
                  ${metrics}
                </td>
              </tr>
              <tr>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px;">
                  Filtered By
                </td>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px; font-weight: bold;">
                  ${filters}
                </td>
              </tr>
              <tr>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px;">
                  Dimensions
                </td>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px; font-weight: bold;">
                  ${dimensionDrillDown}
                </td>
              </tr>
              <tr>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px;">
                  Alert Pattern
                </td>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px; font-weight: bold;">
                  ${alertPattern}
                </td>
              </tr>
              <tr>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px;">
                  Application
                </td>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px; font-weight: bold;">
                  ${application}
                </td>
              </tr>
              <tr>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px;">
                  Mailing list (to)
                </td>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px; font-weight: bold;">
                  ${recipients}
                </td>
              </tr>
              <tr>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px;">
                  Mailing list (cc)
                </td>
                <td colspan="1" style="color: #606060; font-size: 14px; line-height: 20px; font-weight: bold;">
                ${ccRecipients}
                </td>
              </tr>
            </table>
            </td>
          </tr>
          <tr>
            <td colspan="2" style="color: #606060; font-size: 14px; line-height: 20px; text-align: center; padding-bottom: 60px;">
              <a style="color: #FFFFFF; font-size: 16px; font-weight: bold; line-height: 20px; padding: 8px; text-decoration: none; background-color: #0073B1; border-radius: 2px;"
                target="_blank" href="${dashboardHost}/app/#/manage/alert/${functionId}">Review alert</a>
            </td>
          </tr>

          <tr>
            <td style="text-align: center; background-color: #EDF0F3; font-size: 12px; font-family:'Proxima Nova','Arial', 'Helvetica Neue',Helvetica, sans-serif; color: #737373; padding: 24px; font-size:14px;"
              colspan="2">
              <p style="margin-top:0;"> You are receiving this email because you have subscribed to ThirdEye Alert Service for
                <strong>${alertConfigName}</strong>.</p>
              <p>
                If you have any questions regarding this report, please email
                <a style="color: #33aada;" href="mailto:thirdeye-support@startree.ai" target="_top">thirdeye-support@startree.ai</a>
              </p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
