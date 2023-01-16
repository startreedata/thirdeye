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
<section id="filter-dimension-value">
<script id="filter-dimension-value-template" type="text/x-handlebars-template">
   {{#each this}}
    <div class="value-filter" rel="{{@key}}" style="display: none;">
        <label style="display: block;"><input class="filter-select-all-checkbox" type="checkbox">Select All</label>
        <div class="filter-dimension-value-list uk-display-inline-block" style="width:250px;">
            {{#each this}}
            <label class="filter-dimension-value-item" rel="{{@../key}}" value="{{this}}">
                <input class="filter-value-checkbox" type="checkbox" rel="{{@../key}}" value="{{this}}"> {{displayDimensionValue this}}
            </label>
            {{/each}}
        </div>
    </div>
    {{/each}}
</script>
</section>

