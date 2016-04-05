navigation = {
    main(global: true) {
        def index = grailsApplication.config.crm.contact.navigation.index ?: 10
        if(index) {
            crmContact controller: 'crmContact', action: 'index', title: 'crmContact.index.label', order: index
        }
    }
    admin(global: true) {
        crmAddressType controller: 'crmAddressType', action: 'index', order: 110
        crmContactCategoryType controller: 'crmContactCategoryType', action: 'index', order: 120
        crmContactRelationType controller: 'crmContactRelationType', action: 'index', order: 130
    }
}