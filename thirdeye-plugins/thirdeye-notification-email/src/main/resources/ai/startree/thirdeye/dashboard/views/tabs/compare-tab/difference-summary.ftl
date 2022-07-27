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
<section id="heatmap-difference-summary-section" class="hidden" style="margin: 0;">
  <script id="difference-summary" type="text/x-handlebars-template">

  <table>
    <tr>
      <th colspan="3">Dimension</th>
      <th colspan="3">Metrics</th>
    </tr>
    <tr>
       {{#with dimensions}}
       {{#each this as |dimensionName dimensionIndex|}}
        <th>dimensionName</th>
       {{/each}}
       {{/with}}
      <th>Baseline</th>
      <th>Current</th>
      <th>ratio</th>
    </tr>
    {{#with responseRows}}
    {{#each this as |row rowIndex|}}
      <tr>

        <!--{{!--{{#each row.names as |dimensionValue dimension|}}
          <td>{{dimensionValue}}</td>
        {{/each}}--}}-->
        <td>{{row.baselineValue}}</td>
        <td>{{row.currentValue}}</td>
        <td>{{row.ratio}}</td>
      </tr>
    {{/each}}
    {{/with}}
  </table>

  </script>
</section>

