navigation = {
    main(global: true) {
        crmContact controller: 'crmContact', action: 'index', data: [icon: 'person']
    }
    admin(global: true) {
        crmAddressType controller: 'crmAddressType', action: 'index', data: [icon: 'envelope']
        crmContactCategoryType controller: 'crmContactCategoryType', action: 'index', data: [icon: 'person']
        crmContactRelationType controller: 'crmContactRelationType', action: 'index', data: [icon: 'group']
    }
}