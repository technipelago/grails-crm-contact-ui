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

/**
 * Admin controller (CRUD) for CrmContactRelationType.
 */
class CrmContactRelationTypeController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 430,
                    title: 'crmContactRelationType.label',
                    action: 'index'
            ]
    ]

    def selectionService
    def crmContactService

    def domainClass = CrmContactRelationType

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        def baseURI = new URI('gorm://crmContactRelationType/list')
        def query = params.getSelectionQuery()
        def uri

        switch (request.method) {
            case 'GET':
                uri = params.getSelectionURI() ?: selectionService.addQuery(baseURI, query)
                break
            case 'POST':
                uri = selectionService.addQuery(baseURI, query)
                grails.plugins.crm.core.WebUtils.setTenantData(request, 'crmContactRelationTypeQuery', query)
                break
        }

        params.max = Math.min(params.max ? params.int('max') : 20, 100)

        try {
            def result = selectionService.select(uri, params)
            [crmContactRelationTypeList: result, crmContactRelationTypeTotal: result.totalCount, selection: uri]
        } catch (Exception e) {
            flash.error = e.message
            [crmContactRelationTypeList: [], crmContactRelationTypeTotal: 0, selection: uri]
        }
    }

    def create() {
        def crmContactRelationType = crmContactService.createRelationType(params)
        switch (request.method) {
            case 'GET':
                return [crmContactRelationType: crmContactRelationType]
            case 'POST':
                if (!crmContactRelationType.save(flush: true)) {
                    render view: 'create', model: [crmContactRelationType: crmContactRelationType]
                    return
                }

                flash.success = message(code: 'crmContactRelationType.created.message', args: [message(code: 'crmContactRelationType.label', default: 'Relation Type'), crmContactRelationType.toString()])
                redirect action: 'list'
                break
        }
    }

    def edit() {
        switch (request.method) {
            case 'GET':
                def crmContactRelationType = domainClass.get(params.id)
                if (!crmContactRelationType) {
                    flash.error = message(code: 'crmContactRelationType.not.found.message', args: [message(code: 'crmContactRelationType.label', default: 'Relation Type'), params.id])
                    redirect action: 'list'
                    return
                }

                return [crmContactRelationType: crmContactRelationType]
            case 'POST':
                def crmContactRelationType = domainClass.get(params.id)
                if (!crmContactRelationType) {
                    flash.error = message(code: 'crmContactRelationType.not.found.message', args: [message(code: 'crmContactRelationType.label', default: 'Relation Type'), params.id])
                    redirect action: 'list'
                    return
                }

                if (params.version) {
                    def version = params.version.toLong()
                    if (crmContactRelationType.version > version) {
                        crmContactRelationType.errors.rejectValue('version', 'crmContactRelationType.optimistic.locking.failure',
                                [message(code: 'crmContactRelationType.label', default: 'Relation Type')] as Object[],
                                "Another user has updated this Type while you were editing")
                        render view: 'edit', model: [crmContactRelationType: crmContactRelationType]
                        return
                    }
                }

                crmContactRelationType.properties = params

                if (!crmContactRelationType.save(flush: true)) {
                    render view: 'edit', model: [crmContactRelationType: crmContactRelationType]
                    return
                }

                flash.success = message(code: 'crmContactRelationType.updated.message', args: [message(code: 'crmContactRelationType.label', default: 'Relation Type'), crmContactRelationType.toString()])
                redirect action: 'list'
                break
        }
    }

    def delete() {
        def crmContactRelationType = domainClass.get(params.id)
        if (!crmContactRelationType) {
            flash.error = message(code: 'crmContactRelationType.not.found.message', args: [message(code: 'crmContactRelationType.label', default: 'Relation Type'), params.id])
            redirect action: 'list'
            return
        }

        if (isInUse(crmContactRelationType)) {
            render view: 'edit', model: [crmContactRelationType: crmContactRelationType]
            return
        }

        try {
            def tombstone = crmContactRelationType.toString()
            crmContactRelationType.delete(flush: true)
            flash.warning = message(code: 'crmContactRelationType.deleted.message', args: [message(code: 'crmContactRelationType.label', default: 'Relation Type'), tombstone])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'crmContactRelationType.not.deleted.message', args: [message(code: 'crmContactRelationType.label', default: 'Relation Type'), params.id])
            redirect action: 'edit', id: params.id
        }
    }

    private boolean isInUse(CrmContactRelationType type) {
        def count = CrmContactRelation.countByType(type)
        def rval = false
        if (count) {
            flash.error = message(code: "crmContactRelationType.delete.error.reference", args:
                    [message(code: 'crmContactRelationType.label', default: 'Relation Type'),
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
