/*
 * Copyright 2013 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.contact

import grails.plugins.crm.core.TenantUtils

import java.util.regex.Pattern

class CrmContactDecorator {

    def grailsApplication
    def grailsLinkGenerator
    Pattern pattern

    String decorate(String markup, Map params) {
        if (!markup) {
            return markup
        }
        if (pattern == null) {
            synchronized (this) {
                if (pattern == null) {
                    def rx = grailsApplication.config.decorator.agreement.regexp ?: /KONTAKT\-(\S+)/
                    pattern = Pattern.compile(rx)
                }
            }
        }
        markup.replaceAll(pattern) { s, nbr ->
            try {
                def result = CrmContact.createCriteria().get() {
                    projections {
                        property('id')
                        property('name')
                    }
                    eq('tenantId', TenantUtils.tenant)
                    eq('number', nbr)
                    order 'dateCreated', 'desc'
                    maxResults 1
                    cache true
                }
                if (result) {
                    def href = grailsLinkGenerator.link(controller: 'crmContact', action: 'show', id: result[0])
                    s = '<a href="' + href + '">' + result[1] + '</a>'
                }
            } catch (Exception e) {
                // Ignore.
            }
            return s
        }
    }
}
