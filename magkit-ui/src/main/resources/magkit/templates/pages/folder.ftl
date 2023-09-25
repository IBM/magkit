[#--
 #%L
 IBM iX Magnolia Kit
 %%
 Copyright (C) 2023 IBM iX
 %%
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 #L%
--]
<html lang="${cmsfn.language()}">
    <head>
        [@cms.page /]
        <title>${i18n.get('magkit.pages.folder.label')} template</title>
    </head>
    <body>
    [#if cmsfn.editMode]
        <div class="editor-info-large">${i18n.get('folder.text')}</div>
        [#if content.redirect?has_content]
            <p><strong>${i18n.get('magkit.pages.folder.redirect.label')}:</strong> <a href="${cmsfn.link("website", content.redirect)!content.redirect}">link</a></p>
        [/#if]
    [/#if]
    </body>
</html>
