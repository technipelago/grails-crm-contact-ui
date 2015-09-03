navigation = {
    main(global: true) {
        crmContact controller: 'crmContact', action: 'index', order: 10, data: [icon: 'person']
    }
    admin(global: true) {
        crmAddressType controller: 'crmAddressType', action: 'index', order: 110, data: [icon: 'envelope']
        crmContactCategoryType controller: 'crmContactCategoryType', action: 'index', order: 120, data: [icon: 'person']
        crmContactRelationType controller: 'crmContactRelationType', action: 'index', order: 130, data: [icon: 'group']
    }
}