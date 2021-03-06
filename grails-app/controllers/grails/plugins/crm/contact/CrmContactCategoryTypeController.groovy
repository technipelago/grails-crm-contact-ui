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

import grails.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.http.HttpServletResponse

/**
 * Admin controller (CRUD) for CrmContactCategoryType.
 */
class CrmContactCategoryTypeController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def selectionService
    def crmContactService

    def domainClass = CrmContactCategoryType

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        def baseURI = new URI('gorm://crmContactCategoryType/list')
        def query = params.getSelectionQuery()
        def uri

        switch (request.method) {
            case 'GET':
                uri = params.getSelectionURI() ?: selectionService.addQuery(baseURI, query)
                break
            case 'POST':
                uri = selectionService.addQuery(baseURI, query)
                grails.plugins.crm.core.WebUtils.setTenantData(request, 'crmContactCategoryTypeQuery', query)
                break
        }

        params.max = Math.min(params.max ? params.int('max') : 20, 100)

        try {
            def result = selectionService.select(uri, params)
            [crmContactCategoryTypeList: result, crmContactCategoryTypeTotal: result.totalCount, selection: uri]
        } catch (Exception e) {
            flash.error = e.message
            [crmContactCategoryTypeList: [], crmContactCategoryTypeTotal: 0, selection: uri]
        }
    }

    @Transactional
    def create() {
        def crmContactCategoryType = crmContactService.createCategoryType(params)
        switch (request.method) {
            case 'GET':
                return [crmContactCategoryType: crmContactCategoryType]
            case 'POST':
                if (!crmContactCategoryType.save(flush: true)) {
                    render view: 'create', model: [crmContactCategoryType: crmContactCategoryType]
                    return
                }

                flash.success = message(code: 'crmContactCategoryType.created.message', args: [message(code: 'crmContactCategoryType.label', default: 'Category'), crmContactCategoryType.toString()])
                redirect action: 'list'
                break
        }
    }

    @Transactional
    def edit() {
        switch (request.method) {
            case 'GET':
                def crmContactCategoryType = domainClass.get(params.id)
                if (!crmContactCategoryType) {
                    flash.error = message(code: 'crmContactCategoryType.not.found.message', args: [message(code: 'crmContactCategoryType.label', default: 'Category'), params.id])
                    redirect action: 'list'
                    return
                }

                return [crmContactCategoryType: crmContactCategoryType]
            case 'POST':
                def crmContactCategoryType = domainClass.get(params.id)
                if (!crmContactCategoryType) {
                    flash.error = message(code: 'crmContactCategoryType.not.found.message', args: [message(code: 'crmContactCategoryType.label', default: 'Category'), params.id])
                    redirect action: 'list'
                    return
                }

                if (params.version) {
                    def version = params.version.toLong()
                    if (crmContactCategoryType.version > version) {
                        crmContactCategoryType.errors.rejectValue('version', 'crmContactCategoryType.optimistic.locking.failure',
                                [message(code: 'crmContactCategoryType.label', default: 'Category')] as Object[],
                                "Another user has updated this Type while you were editing")
                        render view: 'edit', model: [crmContactCategoryType: crmContactCategoryType]
                        return
                    }
                }

                crmContactCategoryType.properties = params

                if (!crmContactCategoryType.save(flush: true)) {
                    render view: 'edit', model: [crmContactCategoryType: crmContactCategoryType]
                    return
                }

                flash.success = message(code: 'crmContactCategoryType.updated.message', args: [message(code: 'crmContactCategoryType.label', default: 'Category'), crmContactCategoryType.toString()])
                redirect action: 'list'
                break
        }
    }

    @Transactional
    def delete() {
        def crmContactCategoryType = domainClass.get(params.id)
        if (!crmContactCategoryType) {
            flash.error = message(code: 'crmContactCategoryType.not.found.message', args: [message(code: 'crmContactCategoryType.label', default: 'Category'), params.id])
            redirect action: 'list'
            return
        }

        if (isInUse(crmContactCategoryType)) {
            render view: 'edit', model: [crmContactCategoryType: crmContactCategoryType]
            return
        }

        try {
            def tombstone = crmContactCategoryType.toString()
            crmContactCategoryType.delete(flush: true)
            flash.warning = message(code: 'crmContactCategoryType.deleted.message', args: [message(code: 'crmContactCategoryType.label', default: 'Category'), tombstone])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'crmContactCategoryType.not.deleted.message', args: [message(code: 'crmContactCategoryType.label', default: 'Category'), params.id])
            redirect action: 'edit', id: params.id
        }
    }

    private boolean isInUse(CrmContactCategoryType type) {
        def count = CrmContactCategory.countByCategory(type)
        def rval = false
        if (count) {
            flash.error = message(code: "crmContactCategoryType.delete.error.reference", args:
                    [message(code: 'crmContactCategoryType.label', default: 'Category'),
                            message(code: 'crmContact.label', default: 'Contacts'), count],
                    default: "This {0} is used by {1} {2}")
            rval = true
        }
        return rval
    }

    @Transactional
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

    @Transactional
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
