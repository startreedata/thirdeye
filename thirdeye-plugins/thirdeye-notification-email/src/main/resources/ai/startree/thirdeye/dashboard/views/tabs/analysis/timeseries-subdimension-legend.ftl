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
<ul class="analysis-chart__dimensions" id="chart-dimensions">
  {{#if subDimensions}}
    <label class="label-medium-semibold">
      {{#if dimension}}
        {{dimension}}:
      {{else}}
        Dimension:
      {{/if}}
    </label>

    {{#each subDimensions as |subDimension subDimensionIndex|}}
      <li class="analysis-chart__dimension">
        <input class="analysis-chart__checkbox" type="checkbox" id="{{subDimensionIndex}}" {{#if_eq subDimension 'All'}} checked=true {{/if_eq}}>
        <label for="{{subDimensionIndex}}" class="metric-label analysis-change__label">{{subDimension}}</label>
      </li>
    {{/each}}
  {{/if}}
</ul>
