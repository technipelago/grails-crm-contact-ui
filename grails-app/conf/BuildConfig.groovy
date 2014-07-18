grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

grails.project.dependency.resolution = {
    inherits("global") {}
    log "warn"
    legacyResolve false
    repositories {
        grailsCentral()
        mavenCentral()
    }
    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
        // Without xhtmlrenderer and itext we get unresolved dependencies in the Jenkins build!?
        // I tried 'legacyResolve true' but it did not help. Strange...
        compile "org.xhtmlrenderer:core-renderer:R8"
        compile "com.lowagie:itext:2.1.0"
    }

    plugins {
        build(":tomcat:$grailsVersion",
                ":release:2.2.1",
                ":rest-client-builder:1.0.3") {
            export = false
        }
        runtime ":hibernate:$grailsVersion"

        test(":spock:0.7") {
            export = false
            exclude "spock-grails-support"
        }
        test(":codenarc:0.21") { export = false }
        test(":code-coverage:1.2.7") { export = false }

        compile(":avatar:0.6.3") {
            excludes 'xercesImpl', 'xml-apis'
        }

        compile(":qrcode:0.6")
        compile(":rendering:0.4.4")
        compile ":selection:0.9.6"
        compile ":sequence-generator:1.0"
        compile ":selection-repository:0.9.3"
        compile ":recent-domain:0.6.0"
        compile ":decorator:1.1"
        compile ":user-tag:0.6"

        compile ":crm-core:2.0.0"
        compile ":crm-contact:2.0.0"
        compile ":crm-security:2.0.0"
        compile ":crm-tags:2.0.0"
        compile ":crm-feature:2.0.0"
        compile ":crm-ui-bootstrap:2.0.0"
    }
}
