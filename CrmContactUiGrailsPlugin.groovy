/*
 * Copyright 2012 Goran Ehrsson.
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

import grails.plugins.crm.contact.CrmContact

class CrmContactUiGrailsPlugin {
    def groupId = ""
    def version = "2.0.0-SNAPSHOT"
    def grailsVersion = "2.2 > *"
    def dependsOn = [:]
    def loadAfter = ['crmContact']
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "src/groovy/grails/plugins/crm/contact/ContactUiTestSecurityDelegate.groovy"
    ]
    def title = "GR8 CRM Contact Management UI"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''
Provides contact management user interface for GR8 CRM applications.
'''
    def documentation = "https://github.com/gr8crm/grails-crm-contact-ui"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/gr8crm/grails-crm-contact-ui/issues"]
    def scm = [url: "https://github.com/gr8crm/grails-crm-contact-ui"]

    def features = {
        crmContact {
            description "Contact Management"
            link controller: 'crmContact'
            permissions {
                guest "crmContact:index,list,show,createFavorite,deleteFavorite,clearQuery,qrcode,autocompleteTitle,autocompleteCategoryType,autocompleteTags", "qrcode:*"
                partner "crmContact:index,list,show,createFavorite,deleteFavorite,clearQuery,qrcode,autocompleteTitle,autocompleteCategoryType,autocompleteTags", "qrcode:*"
                user "crmContact:*", "qrcode:*"
                admin "crmContact,crmAddressType,crmContactCategoryType,crmContactRelationType:*", "qrcode:*"
            }
            statistics {tenant ->
                def total = CrmContact.countByTenantId(tenant)
                def updated = CrmContact.countByTenantIdAndLastUpdatedGreaterThan(tenant, new Date() - 31)
                def usage
                if (total > 0) {
                    def tmp = updated / total
                    if (tmp < 0.1) {
                        usage = 'low'
                    } else if (tmp < 0.3) {
                        usage = 'medium'
                    } else {
                        usage = 'high'
                    }
                } else {
                    usage = 'none'
                }
                return [usage: usage, objects: total]
            }
        }
    }

    def doWithApplicationContext = { applicationContext ->
        // Add a i18n admin page for this plugin's labels and messages.
        def crmPluginService = applicationContext.crmPluginService
        crmPluginService.registerView('crmMessage', 'index', 'tabs',
                [id: "crmContact", index: 100, label: "crmContact.label",
                        template: '/crmContact/messages', plugin: "crm-contact-lite"]
        )

        // Add contact button in main menu.
        def navigationService = applicationContext.getBean('navigationService')
        navigationService.registerItem('main', [controller: 'crmContact', action: 'index', title: 'crmContact.index.label', order: 10])
        navigationService.updated()
    }

}