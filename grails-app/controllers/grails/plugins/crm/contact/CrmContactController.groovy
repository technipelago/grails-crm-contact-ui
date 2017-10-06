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

import grails.converters.JSON
import grails.converters.XML
import grails.plugins.crm.core.SearchUtils
import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.core.WebUtils
import grails.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.http.HttpServletResponse
import java.util.concurrent.TimeoutException

@Transactional(readOnly = true)
class CrmContactController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], find: 'POST', delete: 'POST', deleteRelation: 'POST']

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

    def filter() {
        def result = [
                [order      : 10,
                 name       : message(code: 'crmContact.filter.activity.label'),
                 description: message(code: 'crmContact.filter.activity.description'),
                 url        : createLink(controller: 'crmTaskFilter', action: 'contact',
                         params: [ns: 'crmTask', topic: 'filterContactActivity'])]
        ]
        render result as JSON
    }

    def export() {
        def user = crmSecurityService.getUserInfo()
        def ns = params.ns ?: 'crmContact'
        if (request.post) {
            def filename = message(code: 'crmContact.label', default: 'Contact')
            try {
                def timeout = (grailsApplication.config.crm.contact.export.timeout ?: 60) * 1000
                def topic = params.topic ?: 'export'
                def result = event(for: ns, topic: topic,
                        data: params + [user: user, tenant: TenantUtils.tenant, locale: request.locale, filename: filename]).waitFor(timeout)?.value
                if (result?.file) {
                    try {
                        WebUtils.inlineHeaders(response, result.contentType, result.filename ?: ns)
                        WebUtils.renderFile(response, result.file)
                    } finally {
                        result.file.delete()
                    }
                    return null // Success
                } else if (result?.redirect) {
                    if (result.error) {
                        flash.error = message(code: result.error)
                    } else if (result.warning) {
                        flash.warning = message(code: result.warning)
                    } else if (result.success || result.message) {
                        flash.success = message(code: (result.success ?: result.message))
                    }
                    redirect result.redirect
                    return
                } else {
                    flash.warning = message(code: 'crmTask.export.nothing.message', default: 'Nothing was exported')
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
            def layouts = event(for: ns, topic: (params.topic ?: 'exportLayout'),
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

    @Transactional
    def company() {
        def tenant = TenantUtils.tenant
        def crmContact = new CrmContact()
        def currentUser = crmSecurityService.getCurrentUser()
        def user = crmSecurityService.getUserInfo(params.username)
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
                bindAddresses(crmContact, params)

                if (!crmContact.save()) {
                    def addressTypes = CrmAddressType.findAllByTenantIdAndEnabled(tenant, true)
                    render(view: 'company', model: [user        : user, crmContact: crmContact,
                                                    addressTypes: crmContact.addresses ? crmContact.addresses*.type : addressTypes, userList: userList, referer: params.referer])
                    return
                }

                event(for: "crmContact", topic: "created", data: [id: crmContact.id, tenant: tenant, user: currentUser?.username, name: crmContact.toString()])

                flash.success = message(code: 'default.created.message', args: [message(code: 'crmContact.label', default: 'Company'), crmContact.toString()])

                if (params.referer) {
                    redirect(uri: params.referer - request.contextPath)
                } else {
                    redirect(action: "show", id: crmContact.id)
                }
                break
        }
    }

    @Transactional
    def contact() {
        def tenant = TenantUtils.tenant
        def crmContact = new CrmContact()
        def currentUser = crmSecurityService.getCurrentUser()
        def user = crmSecurityService.getUserInfo(params.username)
        params.username = user?.username

        bindData(crmContact, params)
        crmContact.tenantId = tenant

        def parentContact = crmContact.parent
        boolean parentCreated = false
        crmContact.parent = null
        def userList = addUserIfMissing(crmSecurityService.getTenantUsers(), crmContact.username)

        switch (request.method) {
            case 'GET':
                def addressTypes = crmContactService.listAddressType(null, [enabled: true])
                def relationTypes = crmContactService.listRelationType(null, [enabled: true])
                return [user        : user, crmContact: crmContact, parentContact: parentContact,
                        addressTypes: addressTypes, relationTypes: relationTypes,
                        userList    : userList, referer: params.referer]
            case 'POST':
                def createPerson = (crmContact.firstName || crmContact.lastName)
                def problem = null
                if (params.parentName && !parentContact) {
                    parentContact = new CrmContact()
                    bindData(parentContact, params, [include: ['telephone']])
                    parentContact.name = params.parentName
                    parentContact.tenantId = tenant
                    bindAddresses(parentContact, params)

                    // If we don't create a person, put the description on the company.
                    if (!createPerson) {
                        parentContact.description = params.description
                    }
                    if (parentContact.save()) {
                        crmContact.addresses?.clear()
                        parentCreated = true
                    } else {
                        problem = parentContact
                    }
                } else {
                    bindAddresses(crmContact, params)
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
                    } else if (parentContact) {
                        crmContactService.addRelation(crmContact, parentContact, params.relationType, true)
                    }
                } else if (parentContact) {
                    crmContact.discard()
                    crmContact = parentContact
                    parentContact = null
                }

                if (problem) {
                    render(view: 'contact', model: [user        : user, crmContact: crmContact, parentContact: parentContact,
                                                    addressTypes: problem.addresses*.type, userList: userList, referer: params.referer])
                    return
                }
                if (parentCreated) {
                    event(for: "crmContact", topic: "created", data: [id: parentContact.id, tenant: tenant, user: currentUser?.username, name: parentContact.toString()])
                }
                event(for: "crmContact", topic: "created", data: [id: crmContact.id, tenant: tenant, user: currentUser?.username, name: crmContact.toString()])

                flash.success = message(code: 'default.created.message', args: [message(code: 'crmContact.label', default: 'Contact'), crmContact.toString()])

                if (params.referer) {
                    redirect(uri: params.referer - request.contextPath)
                } else {
                    redirect(action: "show", id: crmContact.id)
                }
                break
        }
    }

    @Transactional
    def person() {
        def tenant = TenantUtils.tenant
        def crmContact = new CrmContact()
        def currentUser = crmSecurityService.getCurrentUser()
        def user = crmSecurityService.getUserInfo(params.username)
        params.username = user?.username

        bindData(crmContact, params)
        crmContact.tenantId = tenant

        def userList = addUserIfMissing(crmSecurityService.getTenantUsers(), crmContact.username)

        switch (request.method) {
            case 'GET':
                def addressTypes = CrmAddressType.findAllByTenantIdAndEnabled(tenant, true)
                return [user    : user, crmContact: crmContact, addressTypes: addressTypes,
                        userList: userList, referer: params.referer]
            case 'POST':
                bindAddresses(crmContact, params)

                if (!crmContact.save()) {
                    render(view: 'person', model: [user        : user, crmContact: crmContact,
                                                   addressTypes: crmContact.addresses*.type, userList: userList, referer: params.referer])
                    return
                }

                event(for: "crmContact", topic: "created", data: [id: crmContact.id, tenant: tenant, user: currentUser?.username, name: crmContact.toString()])

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
                return [crmContact  : crmContact, children: crmContact.children ?: Collections.EMPTY_LIST, relations: crmContact.relations, primaryRelation: crmContact.primaryRelation,
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

    @Transactional
    def edit(Long id) {
        def tenant = TenantUtils.tenant
        def addressTypes = CrmAddressType.findAllByTenantIdAndEnabled(tenant, true)
        def user = crmSecurityService.getUserInfo(params.username)

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
                return [user     : user, crmContact: crmContact, primaryRelation: primaryRelation, addressTypes: addressTypes,
                        titleList: listDistinctTitle(), userList: userList, referer: params.referer]
            case "POST":
                def userList = addUserIfMissing(crmSecurityService.getTenantUsers(), crmContact.username)
                if (params.version) {
                    def version = params.version.toLong()
                    if (crmContact.version > version) {
                        crmContact.errors.rejectValue("version", "default.optimistic.locking.failure",
                                [message(code: 'crmContact.label', default: 'Contact')] as Object[],
                                "Another user has updated this contact while you were editing")
                        return [user     : user, crmContact: crmContact, primaryRelation: primaryRelation, addressTypes: addressTypes,
                                titleList: listDistinctTitle(), userList: userList, referer: params.referer]
                    }
                }

                bindData(crmContact, params)
                bindCategories(crmContact, params.list('category').findAll { it.trim() })
                bindAddresses(crmContact, params)

                event(for: "crmContact", topic: "bind", data: [id: id, tenant: tenant, user: user?.username, bean: crmContact, params: params], fork: false)

                if (crmContact.hasErrors() || !crmContact.save()) {
                    return [user     : user, crmContact: crmContact, primaryRelation: primaryRelation, addressTypes: addressTypes,
                            titleList: listDistinctTitle(), userList: userList, referer: params.referer]
                }

                flash.success = message(code: 'default.updated.message', args: [message(code: 'crmContact.label', default: 'Contact'), crmContact.toString()])
                event(for: "crmContact", topic: "updated", data: [id: id, tenant: tenant, user: user?.username, name: crmContact.toString()])
                redirect(action: "show", id: crmContact.id)
                break
        }
    }

    private void bindAddresses(CrmContact crmContact, Map params) {
        // This is a workaround for Grails 2.4.4 data binding that does not insert a new CrmContactAddress when 'id' is null.
        // I consider this to be a bug in Grails 2.4.4 but I'm not sure how it's supposed to work with Set.
        // This workaround was not needed in Grails 2.2.4.
        for (i in 0..10) {
            def a = params["addresses[$i]".toString()]
            if (a && !a.id) {
                def ca = new CrmContactAddress(contact: crmContact)
                bindData(ca, a)
                if (!ca.isEmpty()) {
                    if (ca.validate()) {
                        crmContact.addToAddresses(ca)
                    } else {
                        crmContact.errors.addAllErrors(ca.errors)
                    }
                }
            }
        }

        // Remove existing addresses were all properties are blank.
        for (a in crmContact.addresses.findAll { it?.empty }) {
            crmContact.removeFromAddresses(a)
            if (a.id) {
                a.delete()
            }
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

    @Transactional
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

    @Transactional
    def changeType(Long id) {
        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            flash.error = message(code: 'crmContact.not.found.message', args: [message(code: 'crmContact.label', default: 'Contact'), id])
            redirect action: 'index'
            return
        }
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
            if (crmContact.categories) {
                // Remove all categories.
                List removeUs = []
                removeUs.addAll(crmContact.categories)
                for (c in removeUs) {
                    crmContact.removeFromCategories(c)
                    c.delete()
                }
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

    @Transactional
    def createFavorite(Long id) {
        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            flash.error = message(code: 'crmContact.not.found.message', args: [message(code: 'crmContact.label', default: 'Contact'), id])
            redirect action: 'index'
            return
        }
        userTagService.tag(crmContact, getFavoriteTag(), crmSecurityService.currentUser?.username, TenantUtils.tenant)

        redirect(action: 'show', id: params.id)
    }

    @Transactional
    def deleteFavorite(Long id) {
        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            flash.error = message(code: 'crmContact.not.found.message', args: [message(code: 'crmContact.label', default: 'Contact'), id])
            redirect action: 'index'
            return
        }
        userTagService.untag(crmContact, getFavoriteTag(), crmSecurityService.currentUser?.username, TenantUtils.tenant)
        redirect(action: 'show', id: id)
    }

    private String getFavoriteTag() {
        grailsApplication.config.crm.tag.favorite ?: 'favorite'
    }

    @Transactional
    def addRelation(Long id, String type, boolean primary, String description) {
        def crmContact = crmContactService.getContact(id)
        if (!crmContact) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        if (request.post) {
            def related = params.related
            def created = null
            def relatedContact
            if (related?.isNumber()) {
                relatedContact = crmContactService.getContact(Long.valueOf(related))
                if (!relatedContact) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND)
                    return
                }
                if (relatedContact.tenantId != crmContact.tenantId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)
                    return
                }
            } else if (related) {
                def createPerson = crmContact.isCompany()
                if (related.startsWith('@')) {
                    related = related[1..-1]
                    createPerson = true
                } else if (related.endsWith('@')) {
                    related = related[0..-2]
                    createPerson = true
                }
                if (createPerson) {
                    relatedContact = crmContactService.createPerson(firstName: related, true)
                } else {
                    relatedContact = crmContactService.createCompany(name: related, true)
                }
                created = relatedContact
            }

            if (relatedContact) {
                def a = crmContact
                def b = relatedContact
                if (primary && b.isPerson()) {
                    b = crmContact
                    a = relatedContact
                }
                def relation = crmContactService.addRelation(a, b, type, primary, description)
                if (relation.hasErrors()) {
                    flash.warning = message(code: 'crmContactRelation.created.none', default: "No relation created")
                } else {
                    if (created) {
                        def currentUser = crmSecurityService.getCurrentUser()
                        event(for: "crmContact", topic: "created", data: [id: created.id, tenant: created.tenantId, user: currentUser?.username, name: created.toString()])
                    }
                    flash.success = message('crmContactRelation.created.message', default: 'Relation {0} created from {1} to {2}', args: [relation, a, b])
                }
            } else {
                flash.warning = message(code: 'crmContactRelation.created.none', default: "No relation created")
            }
            redirect(action: 'show', id: id, fragment: "relations")
        } else {
            def relation = new CrmContactRelation(a: crmContact)
            // If this person has no existing relations, set property 'primary' to true by default.
            if (crmContact.person && !crmContact.getRelations()) {
                relation.primary = true
            }
            render template: 'addRelation', model: [bean         : relation, crmContact: crmContact,
                                                    relationTypes: crmContactService.listRelationType(null)]
        }
    }

    @Transactional
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
            bindData(relation, params, [include: ['type', 'primary', 'description']])
            relation.save()
            if (params.boolean('primary')) {
                crmContactService.setPrimaryRelation(relation)
            }
            redirect(action: 'show', id: id, fragment: "relations")
        } else {
            render template: 'editRelation', model: [crmContact   : crmContact, bean: relation,
                                                     relationTypes: crmContactService.listRelationType(null)]
        }
    }

    @Transactional
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
        def result = listDistinctTitle(params.q)
        WebUtils.defaultCache(response)
        render result as JSON
    }

    def autocompleteCategoryType() {
        def result = crmContactService.listCategoryType(params.remove('q'), params).collect { it.toString() }
        WebUtils.defaultCache(response)
        render result as JSON
    }

    def autocompleteRelationType() {
        def result = crmContactService.listRelationType(params.remove('q'), params).collect { it.toString() }
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
            handle.object != null && handle.id != id && !result.find { it.id == handle.id }
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
                                                address     : address, row: crmContact.addresses.size()])
    }
}
