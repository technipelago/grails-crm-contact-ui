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

/**
 * Test decorators/CrmAgreementDecorator.groovy
 */
class CrmContactDecoratorSpec extends grails.plugin.spock.IntegrationSpec {

    def grailsApplication
    def crmContactService
    def grailsLinkGenerator

    def "parse text are replace with hyperlinks to crmContact/show"() {
        given: "create company"
        def d = new CrmContactDecorator()
        d.grailsApplication = grailsApplication
        d.grailsLinkGenerator = grailsLinkGenerator
        def c = crmContactService.createCompany(number: "42", name: "Test Company", address: [postalCode: '12345', city: 'Smalltown'], true)

        when: "Parse text"
        def result = d.decorate("KONTAKT-42 is a cool company", [:])

        then:
        result == '<a href="/contact/show/' + c.id + '">Test Company</a> is a cool company'
    }
}
