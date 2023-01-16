/**
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
"use strict";

module.exports = (grunt) => {
  require('load-grunt-tasks')(grunt);

  // Project configuration
  grunt.initConfig({
    babel: {
      options: {
        sourceMap: true,
        presets: ['es2015']
      },
      dist: {
        files: [
          {
            expand: true,
            cwd: 'src/main/resources/assets/javascript/',
            src: ['**/*.js'],
            dest: 'target/classes/assets/javascript/'
          }
        ]
      }
    },

    watch: {
      scripts: {
        files: 'src/main/resources/assets/javascript/**/*.js',
        tasks: ['default']
      }
    }
  });

  grunt.registerTask('default', ['babel']);
};
