// configuration for plugin testing - will not be included in the plugin zip
grails.scaffolding.templates.domainSuffix = ''
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'

    debug 'grails.app.services.grails.plugins.crm.contact'
    debug 'grails.app.selection.grails.plugins.selection'
    debug 'grails.app.selection.grails.plugins.selection'

    debug 'grails.plugins.crm.dataimport'
}