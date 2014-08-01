/*
 *  Copyright 2012 Goran Ehrsson.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package grails.plugins.crm.contact

import org.springframework.dao.DataIntegrityViolationException
import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.core.SearchUtils
import grails.converters.JSON
import grails.plugins.crm.core.WebUtils
import grails.converters.XML

import javax.servlet.http.HttpServletResponse
import java.util.concurrent.TimeoutException

class CrmContactController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], find: 'POST', delete: 'POST', deleteRelation: 'POST']

    static navigation = [
            [group: 'crmContact',
                    order: 10,
                    title: 'crmContact.edit',
                    action: 'edit',
                    isVisible: { actionName == 'show' },
                    id: { params.id }
            ],
            [group: 'crmContact',
                    order: 20,
                    title: 'crmContact.create',
                    action: 'create',
                    isVisible: { actionName != 'create' }
            ],
            [group: 'crmContact',
                    order: 30,
                    title: 'crmContact.find',
                    action: 'index',
                    isVisible: { actionName != 'index' }
            ]
    ]

    def crmContactService
    def crmTagService
    def crmSecurityService
    def selectionService
    def userTagService
    def recentDomainService

    def index() {
        // If any query parameters are specified in the URL, let them override the last query stored in session.
        def cmd = new CrmContactQueryCommand()
        def query = params.getSelectionQuery()
        bindData(cmd, query ?: WebUtils.getTenantData(request, 'crmContactQuery'))
        [cmd: cmd]
    }

    def list() {
        def baseURI = new URI('bean://crmContactService/list')
        def query = params.getSelectionQuery()
        def uri

        switch (request.method) {
            case 'GET':
                uri = params.getSelectionURI() ?: selectionService.addQuery(baseURI, query)
                break
            case 'POST':
                uri = selectionService.addQuery(baseURI, query)
                WebUtils.setTenantData(request, 'crmContactQuery', query)
                break
        }

        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def result
        try {
            result = selectionService.select(uri, params)
            if (result.totalCount == 1 && params.view != 'list') {
                // If we only got one record, show the record immediately.
                redirect action: "show", params: selectionService.createSelectionParameters(uri) + [id: result.head().ident()]
            } else {
                [crmContactList: result, crmContactTotal: result.totalCount, selection: uri]
            }
        } catch (Exception e) {
            flash.error = e.message
            [crmContactList: [], crmContactTotal: 0, selection: uri]
        }
    }

    def clearQuery() {
        WebUtils.setTenantData(request, 'crmContactQuery', null)
        redirect(action: 'index')
    }

    def export() {
        def user = crmSecurityService.getUserInfo()
        def namespace = params.namespace ?: 'crmContact'
        if (request.post) {
            def filename = message(code: 'crmContact.label', default: 'Contact')
            try {
                def topic = params.topic ?: 'export'
                def result = event(for: namespace, topic: topic,
                        data: params + [user: user, tenant: TenantUtils.tenant, locale: request.locale, filename: filename]).waitFor(60000)?.value
                if (result?.file) {
                    try {
                        WebUtils.inlineHeaders(response, result.contentType, result.filename ?: namespace)
                        WebUtils.renderFile(response, result.file)
                    } finally {
                        result.file.delete()
                    }
                    return null // Success
                } else {
                    flash.warning = message(code: 'crmContact.export.nothing.message', default: 'Nothing was exported')
                }
            } catch (TimeoutException te) {
                flash.error = message(code: 'crmContact.export.timeout.message', default: 'Export did not complete')
            } catch (Exception e) {
                log.error("Export event throwed an exception", e)
                flash.error = message(code: 'crmContact.export.error.message', default: 'Export failed due to an error', args: [e.message])
            }
            redirect(action: "index")
        } else {
            def uri = params.getSelectionURI()
            def layouts = event(for: namespace, topic: (params.topic ?: 'exportLayout'),
                    data: [tenant: TenantUtils.tenant, username: user.username, uri: uri]).waitFor(10000)?.values?.flatten()
            [layouts: layouts, selection: uri]
        }
    }

    def create() {
        def linkParams = [:]
        if (params.referer) {
            linkParams.referer = params.referer
        }
        if (params['parent.id']) {
            linkParams['parent.id'] = params['parent.id']
        }
        if (params.type) {
            redirect(action: params.type, params: linkParams)
        } else {
            return [linkParams: linkParams]
        }
    }

    private List addUserIfMissing(List userList, String username) {
        if (username && !userList.find { it.username == username }) {
            userList << [username: username, name: username]
        }
        return userList
    }

    def company() {
        def tenant = TenantUtils.tenant
        def crmContact = new CrmContact()
        def user = crmSecurityService.getUserInfo(params.username) ?: crmSecurityService.getCurrentUser()
        params.username = user?.username
        bindData(crmContact, params)
        crmContact.tenantId = tenant

        def userList = addUserIfMissing(crmSecurityService.getTenantUsers(), crmContact.username)

        switch (request.method) {
            case 'GET':
                def addressTypes = CrmAddressType.findAllByTenantIdAndEnabled(tenant, true)
                return [user: user, crmContact: crmContact, addressTypes: addressTypes, userList: userList, referer: params.referer]
            case 'POST':
                bindCategories(crmContact, params.list('category').findAll { it.trim() })
                for (a in crmContact.addresses.findAll { it.empty }) {
                    crmContact.removeFromAddresses(a)
                    if (a.id) {
                        a.delete()
                    }
                }
                if (!crmContact.save()) {
                    def addressTypes = CrmAddressType.findAllByTenantIdAndEnabled(tenant, true)
                    render(view: 'company', model: [user: user, crmContact: crmContact,
                            addressTypes: crmContact.addresses*.type, userList: userList, referer: params.referer])
                    return
                }
                flash.success = message(code: 'default.created.message', args: [message(code: 'crmContact.label', default: 'Company'), crmContact.toString()])
                if (params.referer) {
                    redirect(uri: params.referer - request.contextPath)
                } else {
                    redirect(action: "show", id: crmContact.id)
                }
                break
        }
    }

    def contact() {
        def tenant = TenantUtils.tenant
        def crmContact = new CrmContact()
        def user = crmSecurityService.getUserInfo(params.username) ?: crmSecurityService.getCurrentUser()
        params.username = user?.username

        bindData(crmContact, params)
        crmContact.tenantId = tenant

        def parentContact = crmContact.parent
        crmContact.parent = null
        def userList = addUserIfMissing(crmSecurityService.getTenantUsers(), crmContact.username)

        switch (request.method) {
            case 'GET':
                def addressTypes = CrmAddressType.findAllByTenantIdAndEnabled(tenant, true)
                return [user: user, crmContact: crmContact, parentContact: parentContact,
                        addressTypes: addressTypes, userList: userList, referer: params.referer]
            case 'POST':
                def createPerson = (crmContact.firstName || crmContact.lastName)
                def problem = null
                CrmContact.withTransaction { tx ->
                    if (params.parentName && !parentContact) {
                        parentContact = new CrmContact()
                        bindData(parentContact, params, [include: ['telephone', 'addresses']])
                        parentContact.name = params.parentName
                        parentContact.tenantId = tenant
                        for (a in parentContact.addresses.findAll { it.empty }) {
                            parentContact.removeFromAddresses(a)
                            if (a.id) {
                                a.delete()
                            }
                        }
                        // If we don't create a person, put the description on the company.
                        if (!createPerson) {
                            parentContact.description = params.description
                        }
                        if (parentContact.save()) {
                            crmContact.addresses?.clear()
                        } else {
                            problem = parentContact
                        }
                    } else {
                        for (a in crmContact.addresses?.findAll { it.empty }) {
                            crmContact.removeFromAddresses(a)
                            if (a.id) {
                                a.delete()
                            }
                        }
                    }

                    // If no username is specified, copy username from parent contact.
                    if ((!crmContact.username) && parentContact?.username) {
                        crmContact.username = parentContact.username
                    }

                    if (createPerson) {
                        if (problem) {
                            crmContact.validate()
                        } else if (!crmContact.save()) {
                            problem = crmContact
                        }
                        if (problem) {
                            crmContact.discard()
                            parentContact?.discard()
                            parentContact = null
                            tx.setRollbackOnly()
                        } else if (parentContact) {
                            crmContactService.addRelation(crmContact, parentContact, null, true)
                        }
                    } else if (parentContact) {
                        crmContact.discard()
                        crmContact = parentContact
                        parentContact = null
                    }
                }
                if (problem) {
                    render(view: 'contact', model: [user: user, crmContact: crmContact, parentContact: parentContact,
                            addressTypes: problem.addresses*.type, userList: userList, referer: params.referer])
                    return
                }
                flash.success = message(code: 'default.created.message', args: [message(code: 'crmContact.label', default: 'Contact'), crmContact.toString()])
                if (params.referer) {
                    redirect(uri: params.referer - request.contextPath)
                } else {
                    redirect(action: "show", id: crmContact.id)
                }
                break
        }
    }

    def person() {
        def tenant = TenantUtils.tenant
        def crmContact = new CrmContact()
        def user = crmSecurityService.getUserInfo(params.username) ?: crmSecurityService.getCurrentUser()
        params.username = user?.username

        bindData(crmContact, params)
        crmContact.tenantId = tenant

        def userList = addUserIfMissing(crmSecurityService.getTenantUsers(), crmContact.username)

        switch (request.method) {
            case 'GET':
                def addressTypes = CrmAddressType.findAllByTenantIdAndEnabled(tenant, true)
                return [user: user, crmContact: crmContact, addressTypes: addressTypes,
                        userList: userList, referer: params.referer]
            case 'POST':
                for (a in crmContact.addresses.findAll { it.empty }) {
                    crmContact.removeFromAddresses(a)
                    if (a.id) {
                        a.delete()
                    }
                }
                if (!crmContact.save()) {
                    render(view: 'person', model: [user: user, crmContact: crmContact,
                            addressTypes: crmContact.addresses*.type, userList: userList, referer: params.referer])
                    return
                }
                flash.success = message(code: 'default.created.message', args: [message(code: 'crmContact.label', default: 'Person'), crmContact.toString()])
                if (params.referer) {
                    redirect(uri: params.referer - request.contextPath)
                } else {
                    redirect(action: "show", id: crmContact.id)
                }
                break
        }
    }

    def show(Long id, String guid) {
        def crmContact = guid ? crmContactService.findByGuid(guid) : crmContactService.getContact(id)
        if (!crmContact) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'crmContact.label', default: 'Contact'), guid ?: id])
            redirect(action: "index")
            return
        }

        withFormat {
            html {
                def externalLink = [:]
                if (crmContact.company) {
                    def externalInfoLink = grailsApplication.config.crm.company.external.info.link
                    if (externalInfoLink) {
                        def externalInfoLabel = grailsApplication.config.crm.company.external.info.label ?: 'crmContact.external.info.label'
                        externalLink.label = message(code: externalInfoLabel, default: "External Information")
                        if (externalInfoLink instanceof Closure) {
                            externalLink.link = externalInfoLink.call(crmContact)
                        } else {
                            externalLink.link = externalInfoLink.toString()
                        }
                    }
                }
                return [crmContact: crmContact, children: crmContact.children ?: Collections.EMPTY_LIST, relations: crmContact.relations, primaryRelation: crmContact.primaryRelation,
                        externalLink: externalLink, selection: params.getSelectionURI()]
            }
            json {
                render crmContact.dao as JSON
            }
            xml {
                render crmContact.dao as XML
            }
        }
    }

    def edit(Long id) {
        def tenant = TenantUtils.tenant
        def addressTypes = CrmAddressType.findAllByTenantIdAndEnabled(tenant, true)
        def user = crmSecurityService.getUserInfo(params.username) ?: crmSecurityService.getCurrentUser()
        params.username = user?.username

        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'crmContact.label', default: 'Contact'), id])
            redirect(action: "index")
            return
        }

        def primaryRelation = crmContact.primaryRelation

        switch (request.method) {
            case "GET":
                def userList = addUserIfMissing(crmSecurityService.getTenantUsers(), crmContact.username)
                return [user: user, crmContact: crmContact, primaryRelation: primaryRelation, addressTypes: addressTypes,
                        titleList: listDistinctTitle(), userList: userList, referer: params.referer]
            case "POST":
                def userList = addUserIfMissing(crmSecurityService.getTenantUsers(), crmContact.username)
                if (params.version) {
                    def version = params.version.toLong()
                    if (crmContact.version > version) {
                        crmContact.errors.rejectValue("version", "default.optimistic.locking.failure",
                                [message(code: 'crmContact.label', default: 'Contact')] as Object[],
                                "Another user has updated this contact while you were editing")
                        return [user: user, crmContact: crmContact, primaryRelation: primaryRelation, addressTypes: addressTypes,
                                titleList: listDistinctTitle(), userList: userList, referer: params.referer]
                    }
                }

                bindData(crmContact, params)
                bindCategories(crmContact, params.list('category').findAll { it.trim() })

                for (a in crmContact.addresses.findAll { it.empty }) {
                    crmContact.removeFromAddresses(a)
                    if (a.id) {
                        a.delete()
                    }
                }
                if (!crmContact.save()) {
                    return [user: user, crmContact: crmContact, primaryRelation: primaryRelation, addressTypes: addressTypes,
                            titleList: listDistinctTitle(), userList: userList, referer: params.referer]
                }

                flash.success = message(code: 'default.updated.message', args: [message(code: 'crmContact.label', default: 'Contact'), crmContact.toString()])
                redirect(action: "show", id: crmContact.id)
                break
        }
    }

    private void bindCategories(CrmContact crmContact, List<String> cats) {
        final Collection<CrmContactCategory> existing = crmContact.categories ?: []
        final List<CrmContactCategoryType> add = []
        final List<CrmContactCategory> remove = []
        for (String c in cats) {
            if (!existing.find { it.toString() == c }) {
                CrmContactCategoryType t = crmContactService.createCategoryType([name: c], true)
                if (t.hasErrors()) {
                    return
                } else {
                    add << t
                }
            }
        }
        for (CrmContactCategory c in existing) {
            if (!cats.find { c.toString() == it }) {
                remove << c
            }
        }
        for (CrmContactCategory c in remove) {
            crmContact.removeFromCategories(c)
            c.delete()
        }
        for (CrmContactCategoryType t in add) {
            crmContact.addToCategories(category: t)
        }
    }

    def delete(Long id) {
        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'crmContact.label', default: 'Contact'), id])
            redirect(action: "index")
            return
        }

        try {
            def tombStone = crmContactService.deleteContact(crmContact)
            flash.warning = message(code: 'default.deleted.message', args: [message(code: 'crmContact.label', default: 'Contact'), tombStone])
            redirect(action: "index")
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'crmContact.label', default: 'Contact'), crmContact])
            redirect(action: "edit", id: id)
        }
    }

    def changeType(Long id) {
        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            flash.error = message(code: 'crmContact.not.found.message', args: [message(code: 'crmContact.label', default: 'Contact'), id])
            redirect action: 'index'
            return
        }
        CrmContact.withTransaction { tx ->
            if (crmContact.company) {
                if (crmContact.children) {
                    flash.error = message(code: 'crmContact.change.type.children.message', args: [message(code: 'crmContact.label', default: 'Contact'), id])
                    redirect(action: 'show', id: id)
                    return
                }
                def names = fixFirstLastName([firstName: crmContact.name])
                crmContact.firstName = names.firstName
                crmContact.lastName = names.lastName
                crmContact.name = null
                // Remove all categories.
                def removeUs = []
                removeUs.addAll(crmContact.categories)
                for (c in removeUs) {
                    crmContact.removeFromCategories(c)
                    c.delete()
                }
            } else {
                crmContact.firstName = null
                crmContact.lastName = null
                // Move mobile number to telephone number field since mobile is not available for companies.
                if (crmContact.mobile && !crmContact.telephone) {
                    crmContact.telephone = crmContact.mobile
                    crmContact.mobile = null
                }
                // name is already set to firstName + ' ' + lastName
            }
            if (crmContact.validate() && crmContact.save()) {
                def newType = crmContact.company ? message(code: 'crmCompany.label', default: 'Company') : message(code: 'crmPerson.label', default: 'Person')
                flash.success = message(code: 'crmContact.change.type.message', args: [crmContact.toString(), newType], default: 'Contact type changed to {0}')
            }
            redirect(action: 'show', id: id)
        }
    }

    private Map fixFirstLastName(final Map params) {
        if (params.firstName && !params.lastName) {
            String[] tmp = params.firstName.split(' ')
            params.firstName = tmp[0]
            if (tmp.length > 1) {
                params.lastName = tmp[1..-1].join(' ')
            }
        }
        return params
    }

    def createFavorite(Long id) {
        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            flash.error = message(code: 'crmContact.not.found.message', args: [message(code: 'crmContact.label', default: 'Contact'), id])
            redirect action: 'index'
            return
        }
        userTagService.tag(crmContact, grailsApplication.config.crm.tag.favorite, crmSecurityService.currentUser?.username, TenantUtils.tenant)

        redirect(action: 'show', id: params.id)
    }

    def deleteFavorite(Long id) {
        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            flash.error = message(code: 'crmContact.not.found.message', args: [message(code: 'crmContact.label', default: 'Contact'), id])
            redirect action: 'index'
            return
        }
        userTagService.untag(crmContact, grailsApplication.config.crm.tag.favorite, crmSecurityService.currentUser?.username, TenantUtils.tenant)
        redirect(action: 'show', id: id)
    }

    def addRelation(Long id, String type, boolean primary, String description) {
        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        if (request.post) {
            def related = params.related
            def relatedContact
            if(related?.isNumber()) {
                relatedContact = crmContactService.getContact(Long.valueOf(related))
                if (!relatedContact) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND)
                    return
                }
                if (relatedContact.tenantId != crmContact.tenantId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)
                    return
                }
            } else if(related) {
                if(related.startsWith('@')) {
                    relatedContact = crmContactService.createPerson(firstName: related[1..-1], true)
                } else if(related.endsWith('@')) {
                    relatedContact = crmContactService.createPerson(firstName: related[0..-2], true)
                } else {
                    relatedContact = crmContactService.createCompany(name: related, true)
                }
            }

            if (relatedContact) {
                def relation = crmContactService.addRelation(crmContact, relatedContact, type, primary, description)
                flash.success = "Relation ${relation} skapad mellan $crmContact och $relatedContact"
            } else {
                flash.warning = "No relation created"
            }
            redirect(action: 'show', id: id, fragment: "relations")
        } else {
            def relation = new CrmContactRelation(a: crmContact)
            // If this person has no existing relations, set property 'primary' to true by default.
            if(crmContact.person && ! crmContact.getRelations()) {
                relation.primary = true
            }
            render template: 'addRelation', model: [bean: relation, crmContact: crmContact,
                    relationTypes: crmContactService.listRelationType(null)]
        }
    }

    def editRelation(Long id, Long r) {
        def crmContact = crmContactService.getContact(id)
        def relation = CrmContactRelation.get(r)
        if (!(relation && crmContact)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        if (relation.a.id != id && relation.b.id != id) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }
        if (request.post) {
            CrmContactRelation.withTransaction {
                bindData(relation, params, [include: ['type', 'primary', 'description']])
                relation.save()
                if (params.boolean('primary')) {
                    crmContactService.setPrimaryRelation(relation)
                }
            }
            redirect(action: 'show', id: id, fragment: "relations")
        } else {
            render template: 'editRelation', model: [crmContact: crmContact, bean: relation,
                    relationTypes: crmContactService.listRelationType(null)]
        }
    }

    def deleteRelation(Long id, Long r) {
        def relation = CrmContactRelation.get(r)
        if (!relation) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        if (relation.a.id != id && relation.b.id != id) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }
        def tombstone = relation.toString()
        def contactA = relation.a.toString()
        def contactB = relation.b.toString()
        def msg = message(code: 'crmContactRelation.deleted.message', default: 'Relation {1} between {2} and {3} deleted',
                args: ['Relation', tombstone, contactA, contactB])

        relation.delete(flush: true)

        flash.warning = msg

        if (request.xhr) {
            def result = [id: id, r: r, message: msg, relation: tombstone, contacts: [contactA, contactB]]
            render result as JSON
        } else {
            redirect(action: 'show', id: id, fragment: "relations")
        }
    }

    def qrcode(Long id) {
        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        WebUtils.defaultCache(response)
        render template: 'qrcode', model: [crmContact: crmContact]
    }

    def autocompleteUsername() {
        def query = params.q?.toLowerCase()
        def list = crmSecurityService.getTenantUsers().findAll { user ->
            if (query) {
                return user.name.toLowerCase().contains(query) || user.username.toLowerCase().contains(query)
            }
            return true
        }.collect { user ->
            [id: user.username, text: user.name]
        }
        def result = [q: params.q, timestamp: System.currentTimeMillis(), length: list.size(), more: false, results: list]
        WebUtils.defaultCache(response)
        render result as JSON
    }

    def autocompleteUsernameSimple() {
        def query = params.q?.toLowerCase()
        def result = crmSecurityService.getTenantUsers().findAll { user ->
            if (query) {
                return user.name.toLowerCase().contains(query) || user.username.toLowerCase().contains(query)
            }
            return true
        }.collect { user ->
            [label: user.name, value: user.username]
        }
        WebUtils.defaultCache(response)
        render result as JSON
    }

    private List listDistinctTitle(String filter = null) {
        CrmContact.createCriteria().list {
            projections {
                distinct('title')
            }
            eq('tenantId', TenantUtils.tenant)
            if (filter) {
                ilike('title', SearchUtils.wildcard(filter))
            } else {
                isNotNull('title')
            }
        }
    }

    def autocompleteTitle() {
        def result = listDistinctTitle(params.term)
        WebUtils.defaultCache(response)
        render result as JSON
    }

    def autocompleteCategoryType() {
        def result = crmContactService.listCategoryType(params.remove('term'), params).collect { it.toString() }
        WebUtils.defaultCache(response)
        render result as JSON
    }

    def autocompleteTags() {
        params.offset = params.offset ? params.int('offset') : 0
        if (params.limit && !params.max) params.max = params.limit
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        def result = crmTagService.listDistinctValue(CrmContact.name, params.remove('q'), params)
        WebUtils.defaultCache(response)
        render result as JSON
    }

    private List listCompanies(String filter = null, Integer max = null) {
        CrmContact.createCriteria().list() {
            projections {
                property('name')
                property('id')
            }
            isNull('firstName')
            isNull('lastName')
            if (filter) {
                ilike('name', SearchUtils.wildcard(filter))
            }
            eq('tenantId', TenantUtils.tenant)
            maxResults(max ?: 10)
        }
    }

    def autocompleteCompany(Long id) {
        def result = listCompanies(params.q, params.int('limit'))
        if (id) {
            result = result.findAll { it[1] != id }
        }
        WebUtils.noCache(response)
        render result as JSON
    }

    def autocompleteContact(Long id, String q) {
        def result = CrmContact.createCriteria().list() {
            eq('tenantId', TenantUtils.tenant)
            if (q) {
                ilike('name', SearchUtils.wildcard(q))
            }
            if (id) {
                ne('id', id)
            }
            maxResults(params.int('limit') ?: 10)
        }.collect { [id: it.id, name: it.fullName, address: it.address.toString()] }

        // Append most recent viewed contacts.
        def recentContacts = recentDomainService.getHistory(request, CrmContact)?.findAll { handle ->
            handle.id != id && !result.find { it.id == handle.id }
        }.collect {
            def obj = it.object
            [id: it.id, name: obj.fullName, address: obj.address.toString(), recent: true]
        }
        result.addAll(recentContacts)
        WebUtils.noCache(response)
        render result as JSON
    }

    def selectAddressType(Long id) {
        def tenant = TenantUtils.tenant
        def crmContact = crmContactService.getContact(id)
        def addressTypes = CrmAddressType.findAllByTenantIdAndEnabled(tenant, true)
        WebUtils.noCache(response)
        render(template: "addAddressModal", model: [addressTypes: addressTypes, crmContact: crmContact])
    }

    def addAddress(Long id) {
        def tenant = TenantUtils.tenant
        def crmContact = crmContactService.getContact(id)
        def addressTypes = CrmAddressType.findAllByTenantIdAndEnabled(tenant, true)
        def address = new CrmContactAddress(contact: crmContact)
        WebUtils.noCache(response)
        render(template: "address-add", model: [addressTypes: addressTypes,
                address: address, row: crmContact.addresses.size()])
    }
}
