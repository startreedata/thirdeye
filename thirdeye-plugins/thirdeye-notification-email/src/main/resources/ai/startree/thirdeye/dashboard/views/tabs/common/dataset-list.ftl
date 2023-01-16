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
<script id="datasets-template" type="text/x-handlebars-template">
    <ul class="uk-nav uk-nav-dropdown single-select dataset-options radio-options">
        {{#with data}}
        {{#each this}}
        <li class="dataset-option{{@root/scope}}" rel="dataset" value="{{this}}"><a href="#">{{this}}</a></li>
        {{/each}}
        {{/with}}
    </ul>
</script>