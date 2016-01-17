package grails.plugins.crm.contact

import grails.plugins.crm.core.TenantUtils
import org.springframework.web.servlet.support.RequestContextUtils

/**
 * List possible duplicate contacts by triggering a synchronous event that fetch contacts.
 */
class CrmContactDuplicatesController {

    static allowedMethods = [list: 'POST']

    def crmSecurityService

    /**
     * Trigger event 'crmContact.duplicates' and wait for reply (that should be a List of CrmContact instances).
     * @return
     */
    def list() {
        def user = crmSecurityService.getUserInfo(null)
        def locale = RequestContextUtils.getLocale(request) ?: Locale.getDefault()
        def timeout = (grailsApplication.config.crm.contact.duplicates.timeout ?: 60) * 1000
        def result = event(for: 'crmContact', topic: 'duplicates', fork: false,
                data: params + [user: user, tenant: TenantUtils.tenant, locale: locale]).waitFor(timeout)?.value ?: []

        try {
            [crmContactList: result, crmContactTotal: result.size()]
        } catch (Exception e) {
            flash.error = e.message
            [crmContactList: [], crmContactTotal: 0]
        }
    }
}
