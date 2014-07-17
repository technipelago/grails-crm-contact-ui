/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

package grails.plugins.crm.contact

import grails.validation.Validateable

/**
 * CrmContact DAO
 */
@Validateable
class CrmContactCommand implements Serializable {

    Long id
    Integer version
    CrmContact parent
    String firstName
    String lastName
    String name
    String number
    String ssn
    String description
    String picture
    Integer birthYear
    Integer birthMonth
    Integer birthDay
    String title

    CrmContactAddress address

    String email
    String telephone
    String mobile
    String url

    static constraints = {
        importFrom CrmContact, exclude: ['number']
        number(maxSize:16, nullable:true)
    }

    public static final List PROPS = ['firstName', 'lastName', 'number', 'ssn', 'description', 'picture',
            'birthYear', 'birthMonth', 'birthDay', 'title', 'email', 'telephone', 'mobile', 'url']

    public static final List ADDR_PROPS = ['address1', 'address2', 'address3', 'postalCode', 'city', 'region', 'country', 'timezone', 'latitude', 'longitude']

    public CrmContactCommand() {

    }

    public CrmContactCommand(CrmContact crmContact) {
        id = crmContact.id
        version = crmContact.version

        if (crmContact.company) {
            name = crmContact.name
            firstName = null
            lastName = null
        } else if (crmContact.parent) {
            parent = crmContact.parent
            name = crmContact.parent.name
            firstName = crmContact.firstName
            lastName = crmContact.lastName
        } else {
            parent = null
            name = null
            firstName = crmContact.firstName
            lastName = crmContact.lastName
        }

        for (prop in PROPS) {
            this."$prop" = crmContact."$prop"
        }
        def a = crmContact.address
        if (a) {
            address = new CrmContactAddress()
            for (prop in ADDR_PROPS) {
                address."$prop" = a."$prop"
            }
        }
    }

    String toString() {
        def s = new StringBuilder()
        if (firstName || lastName) {
            if (firstName) {
                s << firstName
            }
            if (lastName) {
                if (s.length() > 0) {
                    s << ' '
                }
                s << lastName
            }
        } else if(name) {
            s << name
        }
        s.toString()
    }
}
