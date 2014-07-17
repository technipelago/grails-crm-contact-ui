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

import org.springframework.dao.DataIntegrityViolationException
import javax.servlet.http.HttpServletResponse

class CrmAddressTypeController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 410,
                    title: 'crmAddressType.label',
                    action: 'index'
            ]
    ]

    def selectionService
    def crmContactService

    def domainClass = CrmAddressType

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        def baseURI = new URI('gorm://crmAddressType/list')
        def query = params.getSelectionQuery()
        def uri

        switch (request.method) {
            case 'GET':
                uri = params.getSelectionURI() ?: selectionService.addQuery(baseURI, query)
                break
            case 'POST':
                uri = selectionService.addQuery(baseURI, query)
                grails.plugins.crm.core.WebUtils.setTenantData(request, 'crmAddressTypeQuery', query)
                break
        }

        params.max = Math.min(params.max ? params.int('max') : 20, 100)

        try {
            def result = selectionService.select(uri, params)
            [crmAddressTypeList: result, crmAddressTypeTotal: result.totalCount, selection: uri]
        } catch (Exception e) {
            flash.error = e.message
            [crmAddressTypeList: [], crmAddressTypeTotal: 0, selection: uri]
        }
    }

    def create() {
        def crmAddressType = crmContactService.createAddressType(params)
        switch (request.method) {
            case 'GET':
                return [crmAddressType: crmAddressType]
            case 'POST':
                if (!crmAddressType.save(flush: true)) {
                    render view: 'create', model: [crmAddressType: crmAddressType]
                    return
                }

                flash.success = message(code: 'crmAddressType.created.message', args: [message(code: 'crmAddressType.label', default: 'Address Type'), crmAddressType.toString()])
                redirect action: 'list'
                break
        }
    }

    def edit() {
        switch (request.method) {
            case 'GET':
                def crmAddressType = domainClass.get(params.id)
                if (!crmAddressType) {
                    flash.error = message(code: 'crmAddressType.not.found.message', args: [message(code: 'crmAddressType.label', default: 'Address Type'), params.id])
                    redirect action: 'list'
                    return
                }

                return [crmAddressType: crmAddressType]
            case 'POST':
                def crmAddressType = domainClass.get(params.id)
                if (!crmAddressType) {
                    flash.error = message(code: 'crmAddressType.not.found.message', args: [message(code: 'crmAddressType.label', default: 'Address Type'), params.id])
                    redirect action: 'list'
                    return
                }

                if (params.version) {
                    def version = params.version.toLong()
                    if (crmAddressType.version > version) {
                        crmAddressType.errors.rejectValue('version', 'crmAddressType.optimistic.locking.failure',
                                [message(code: 'crmAddressType.label', default: 'Address Type')] as Object[],
                                "Another user has updated this Type while you were editing")
                        render view: 'edit', model: [crmAddressType: crmAddressType]
                        return
                    }
                }

                crmAddressType.properties = params

                if (!crmAddressType.save(flush: true)) {
                    render view: 'edit', model: [crmAddressType: crmAddressType]
                    return
                }

                flash.success = message(code: 'crmAddressType.updated.message', args: [message(code: 'crmAddressType.label', default: 'Address Type'), crmAddressType.toString()])
                redirect action: 'list'
                break
        }
    }

    def delete() {
        def crmAddressType = domainClass.get(params.id)
        if (!crmAddressType) {
            flash.error = message(code: 'crmAddressType.not.found.message', args: [message(code: 'crmAddressType.label', default: 'Address Type'), params.id])
            redirect action: 'list'
            return
        }

        if (isInUse(crmAddressType)) {
            render view: 'edit', model: [crmAddressType: crmAddressType]
            return
        }

        try {
            def tombstone = crmAddressType.toString()
            crmAddressType.delete(flush: true)
            flash.warning = message(code: 'crmAddressType.deleted.message', args: [message(code: 'crmAddressType.label', default: 'Address Type'), tombstone])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'crmAddressType.not.deleted.message', args: [message(code: 'crmAddressType.label', default: 'Address Type'), params.id])
            redirect action: 'edit', id: params.id
        }
    }

    private boolean isInUse(CrmAddressType type) {
        def count = CrmContactAddress.countByType(type)
        def rval = false
        if (count) {
            flash.error = message(code: "crmAddressType.delete.error.reference", args:
                    [message(code: 'crmAddressType.label', default: 'Address Type'),
                            message(code: 'crmContact.label', default: 'Contacts'), count],
                    default: "This {0} is used by {1} {2}")
            rval = true
        }
        return rval
    }

    def moveUp(Long id) {
        def target = domainClass.get(id)
        if (target) {
            def sort = target.orderIndex
            def prev = domainClass.createCriteria().list([sort: 'orderIndex', order: 'desc']) {
                lt('orderIndex', sort)
                maxResults 1
            }?.find { it }
            if (prev) {
                domainClass.withTransaction { tx ->
                    target.orderIndex = prev.orderIndex
                    prev.orderIndex = sort
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
        redirect action: 'list'
    }

    def moveDown(Long id) {
        def target = domainClass.get(id)
        if (target) {
            def sort = target.orderIndex
            def next = domainClass.createCriteria().list([sort: 'orderIndex', order: 'asc']) {
                gt('orderIndex', sort)
                maxResults 1
            }?.find { it }
            if (next) {
                domainClass.withTransaction { tx ->
                    target.orderIndex = next.orderIndex
                    next.orderIndex = sort
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
        redirect action: 'list'
    }
}
