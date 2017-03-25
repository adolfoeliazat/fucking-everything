package aps.back

import aps.*

@Servant class ServeRecreateTestDatabaseSchema : BitchyProcedure() {
    override fun serve() {
        fuckDangerously(FuckDangerouslyParams(
            bpc = bpc, makeRequest = {RecreateTestDatabaseSchemaRequest()},
            runShit = fun(ctx, req): GenericResponse {
                backPlatform.recreateDBSchema() // New context -- new EMF
                enhanceDB()
                seed()
                return GenericResponse()
            }
        ))
    }

    fun seed() {
        backPlatform.userRepo.save(User(user = UserFields(email = "dasja@test.shit.ua", firstName = "Дася", lastName = "Админовна", profilePhone = "911", kind = UserKind.ADMIN, state = UserState.COOL, passwordHash = backPlatform.hashPassword("dasja-secret"), adminNotes = "", subscribedToAllCategories = false)))

        fun makeCategory(title: String, id: Long, children: List<UADocumentCategory> = listOf()) =
            UADocumentCategory(UADocumentCategoryFields(title = title, parent = null, children = children.toMutableList()))-{o->
                o.imposedIDToGenerate = id
            }

        val ids = const.uaDocumentCategoryID
        val root = makeCategory("ROOT", ids.root, listOf(
            makeCategory(t("TOTE", "Разное"), ids.misc),
            makeCategory(t("TOTE", "Экономические"), ids.economyGroup, listOf(
                makeCategory(t("TOTE", "Аудит"), ids.auditing),
                makeCategory(t("TOTE", "Банковское дело"), ids.banking),
                makeCategory(t("TOTE", "Биржевое дело"), ids.stockExchange),
                makeCategory(t("TOTE", "Бухгалтерский учет"), ids.accounting),
                makeCategory(t("TOTE", "Бюджетная система"), ids.budgetSystem),
                makeCategory(t("TOTE", "Валютное регулирование и контроль"), ids.currencyRegulation),
                makeCategory(t("TOTE", "Валютные отношения"), ids.currencyRelationships),
                makeCategory(t("TOTE", "Деньги и кредит"), ids.moneyAndCredit),
                makeCategory(t("TOTE", "Государственная служба"), ids.publicService),
                makeCategory(t("TOTE", "Государственное управление"), ids.publicAdministration),
                makeCategory(t("TOTE", "Государственные финансы"), ids.publicFinances),
                makeCategory(t("TOTE", "Делопроизводство, документоведение, документалистика"), ids.documentManagement),
                makeCategory(t("TOTE", "Эконометрика"), ids.econometrics),
                makeCategory(t("TOTE", "Экономика"), ids.economy),
                makeCategory(t("TOTE", "Экономика предприятий"), ids.enterpriseEconomics),
                makeCategory(t("TOTE", "Экономика труда и социально-трудовые отношения"), ids.laborEconomics),
                makeCategory(t("TOTE", "Экономическая кибернетика"), ids.economicCybernetics),
                makeCategory(t("TOTE", "Экономический анализ"), ids.economicAnalysis),
                makeCategory(t("TOTE", "Электронная коммерция"), ids.eCommerce),
                makeCategory(t("TOTE", "Связи с общественностью, PR"), ids.pr),
                makeCategory(t("TOTE", "Внешнеэкономическая деятельность, ВЭД"), ids.foreignTradeActivities),
                makeCategory(t("TOTE", "Инвестирование, инвестиционная деятельность"), ids.investment),
                makeCategory(t("TOTE", "Инновационная деятельность"), ids.innovativeActivity),
                makeCategory(t("TOTE", "Инновационный менеджмент"), ids.innovativeManagement),
                makeCategory(t("TOTE", "Казначейское дело"), ids.treasury),
                makeCategory(t("TOTE", "Контроллинг"), ids.control),
                makeCategory(t("TOTE", "Лесное хозяйство"), ids.forestry),
                makeCategory(t("TOTE", "Логистика"), ids.logistics),
                makeCategory(t("TOTE", "Макроэкономика, государственное регулирование экономики"), ids.macroeconomics),
                makeCategory(t("TOTE", "Маркетинг, рекламная деятельность"), ids.marketingAndAdvertisement),
                makeCategory(t("TOTE", "Менеджмент, управление персоналом"), ids.management),
                makeCategory(t("TOTE", "Таможенное дело"), ids.customs),
                makeCategory(t("TOTE", "Международная экономика и международные экономические отношения"), ids.internationalEconomics),
                makeCategory(t("TOTE", "Микроэкономика"), ids.microeconomics),
                makeCategory(t("TOTE", "Моделирование экономики"), ids.economicModeling),
                makeCategory(t("TOTE", "Налогообложение, налоги, налоговая система"), ids.taxes),
                makeCategory(t("TOTE", "Предпринимательство"), ids.entrepreneurship),
                makeCategory(t("TOTE", "Политэкономия, экономическая теория, история экономических учений"), ids.politicalEconomy),
                makeCategory(t("TOTE", "Ресторанно-гостиничный бизнес, бытовое обслуживание"), ids.restaurantHotelBusinessAndConsumerService),
                makeCategory(t("TOTE", "Рынок ценных бумаг"), ids.securitiesMarket),
                makeCategory(t("TOTE", "Размещение производительных сил, региональная экономика, экономическая география, РПС"), ids.locationOfProductiveForces),
                makeCategory(t("TOTE", "Сельское хозяйство и агропромышленный комплекс"), ids.agriculture),
                makeCategory(t("TOTE", "Стандартизация, управление качеством"), ids.standardizationAndQualityManagement),
                makeCategory(t("TOTE", "Статистика"), ids.statistics),
                makeCategory(t("TOTE", "Стратегический менеджмент"), ids.strategicManagement),
                makeCategory(t("TOTE", "Страхование, страховое дело"), ids.insurance),
                makeCategory(t("TOTE", "Товароведение и экспертиза"), ids.commodityAndExpertise),
                makeCategory(t("TOTE", "Торговля и коммерческая деятельность"), ids.tradeAndCommercialActivity),
                makeCategory(t("TOTE", "Туризм"), ids.tourism),
                makeCategory(t("TOTE", "Управление проектами"), ids.projectManagement),
                makeCategory(t("TOTE", "Управленческий учет"), ids.managementAccounting),
                makeCategory(t("TOTE", "Финансы"), ids.finance),
                makeCategory(t("TOTE", "Финансы предприятий"), ids.enterpriseFinance),
                makeCategory(t("TOTE", "Финансовый анализ"), ids.financialAnalysis),
                makeCategory(t("TOTE", "Финансовый менеджмент"), ids.financialManagement),
                makeCategory(t("TOTE", "Ценообразование"), ids.pricing)
            )),

            makeCategory(t("TOTE", "Естественные"), ids.naturalGroup, listOf(
                makeCategory(t("TOTE", "Астрономия"), ids.astronomy),
                makeCategory(t("TOTE", "Биология"), ids.biology),
                makeCategory(t("TOTE", "Военная подготовка"), ids.militaryFucking),
                makeCategory(t("TOTE", "География"), ids.geography),
                makeCategory(t("TOTE", "Геодезия"), ids.geodesy),
                makeCategory(t("TOTE", "Геология"), ids.geology),
                makeCategory(t("TOTE", "Экология"), ids.ecology),
                makeCategory(t("TOTE", "Математика"), ids.math),
                makeCategory(t("TOTE", "Медицина"), ids.medicine),
                makeCategory(t("TOTE", "Естествознание"), ids.naturalHistory),
                makeCategory(t("TOTE", "Фармацевтика"), ids.pharmaceuticals),
                makeCategory(t("TOTE", "Физика"), ids.physics),
                makeCategory(t("TOTE", "Химия"), ids.chemistry)
            )),

            makeCategory(t("TOTE", "Технические"), ids.technicalGroup, listOf(
                makeCategory(t("TOTE", "Авиация и космонавтика"), ids.aviationAndCosmonautics),
                makeCategory(t("TOTE", "Архитектура"), ids.architecture),
                makeCategory(t("TOTE", "Базы данных"), ids.databases),
                makeCategory(t("TOTE", "Строительство"), ids.construction),
                makeCategory(t("TOTE", "Электроника"), ids.electronics),
                makeCategory(t("TOTE", "Электротехника"), ids.electricalEngineering),
                makeCategory(t("TOTE", "Информатика и вычислительная техника"), ids.informaticsAndComputing),
                makeCategory(t("TOTE", "Информационная безопасность"), ids.informationSecurity),
                makeCategory(t("TOTE", "Информационно-аналитическая деятельность"), ids.informationAnalyticalActivity),
                makeCategory(t("TOTE", "Кибернетика"), ids.cybernetics),
                makeCategory(t("TOTE", "Чертежи"), ids.drawings),
                makeCategory(t("TOTE", "Программирование"), ids.programming),
                makeCategory(t("TOTE", "Проектирование"), ids.technicalDesign),
                makeCategory(t("TOTE", "Радиоэлектроника, радиотехника"), ids.radioEngineering),
                makeCategory(t("TOTE", "Теоретическая механика"), ids.theoreticalMechanics),
                makeCategory(t("TOTE", "Теория механизмов и машин (ТММ), детали машин (ДМ)"), ids.theoryOfMechanismsAndMachines),
                makeCategory(t("TOTE", "Теплотехника"), ids.heatEngineering),
                makeCategory(t("TOTE", "Технологии, система технологий"), ids.technologySystem),
                makeCategory(t("TOTE", "Технология машиностроения"), ids.engineeringTechnology),
                makeCategory(t("TOTE", "Технология приготовления пищи"), ids.cookingTechnology),
                makeCategory(t("TOTE", "Транспортное строительство"), ids.transportConstruction)
            )),

            makeCategory(t("TOTE", "Юридические"), ids.legalGroup, listOf(
                makeCategory(t("TOTE", "Адвокатура"), ids.advocacy),
                makeCategory(t("TOTE", "Административное право"), ids.administrativeLaw),
                makeCategory(t("TOTE", "Арбитражный процесс"), ids.arbitrationProceedings),
                makeCategory(t("TOTE", "Хозяйственное право"), ids.economicLaw),
                makeCategory(t("TOTE", "Экологическое право"), ids.environmentalLaw),
                makeCategory(t("TOTE", "Жилищное право"), ids.housingLaw),
                makeCategory(t("TOTE", "Земельное право"), ids.landLaw),
                makeCategory(t("TOTE", "История государства и права"), ids.historyOfStateAndLaw),
                makeCategory(t("TOTE", "Конституционное право"), ids.constitutionalLaw),
                makeCategory(t("TOTE", "Корпоративное право"), ids.corporateLaw),
                makeCategory(t("TOTE", "Криминалистика, экспертиза"), ids.forensics),
                makeCategory(t("TOTE", "Уголовное право, криминология"), ids.criminalLaw),
                makeCategory(t("TOTE", "Уголовный процесс"), ids.criminalProcess),
                makeCategory(t("TOTE", "Таможенное право"), ids.customsLaw),
                makeCategory(t("TOTE", "Международное право"), ids.internationalLaw),
                makeCategory(t("TOTE", "Муниципальное право"), ids.municipalLaw),
                makeCategory(t("TOTE", "Нотариат"), ids.notary),
                makeCategory(t("TOTE", "Предпринимательское право"), ids.businessLaw),
                makeCategory(t("TOTE", "Налоговое право"), ids.taxLaw),
                makeCategory(t("TOTE", "Право"), ids.law),
                makeCategory(t("TOTE", "Право интеллектуальной собственности"), ids.intellectualPropertyRights),
                makeCategory(t("TOTE", "Семейное право"), ids.familyLaw),
                makeCategory(t("TOTE", "Страховое право"), ids.insuranceLaw),
                makeCategory(t("TOTE", "Судебные и правоохранительные органы"), ids.judicialAndLawEnforcementAgencies),
                makeCategory(t("TOTE", "Судебно-медицинская экспертиза"), ids.forensicMedicalExamination),
                makeCategory(t("TOTE", "Теория государства и права"), ids.theoryOfStateAndLaw),
                makeCategory(t("TOTE", "Трудовое право"), ids.laborLaw),
                makeCategory(t("TOTE", "Финансовое право"), ids.financialLaw),
                makeCategory(t("TOTE", "Гражданское право"), ids.civilLaw)
            )),

            makeCategory(t("TOTE", "Гуманитарные"), ids.humanitarianGroup, listOf(
                makeCategory(t("TOTE", "Анализ банковской деятельности"), ids.analysisOfBankingActivities),
                makeCategory(t("TOTE", "Английский язык"), ids.english),
                makeCategory(t("TOTE", "Безопасность жизнедеятельности (БЖД)"), ids.lifeSafety),
                makeCategory(t("TOTE", "Дизайн"), ids.design),
                makeCategory(t("TOTE", "Дипломатия"), ids.diplomacy),
                makeCategory(t("TOTE", "Эстетика"), ids.aesthetics),
                makeCategory(t("TOTE", "Этика"), ids.ethics),
                makeCategory(t("TOTE", "Журналистика и издательское дело"), ids.journalismAndPublishing),
                makeCategory(t("TOTE", "История"), ids.history),
                makeCategory(t("TOTE", "Краеведение"), ids.localAreaStudies),
                makeCategory(t("TOTE", "Культурология"), ids.culture),
                makeCategory(t("TOTE", "Лингвистика"), ids.linguistics),
                makeCategory(t("TOTE", "Литература зарубежная"), ids.foreignLiterature),
                makeCategory(t("TOTE", "Литература русский"), ids.russianLiterature),
                makeCategory(t("TOTE", "Литература украинский"), ids.ukrainianLiterature),
                makeCategory(t("TOTE", "Логика"), ids.logic),
                makeCategory(t("TOTE", "Искусство и культура"), ids.artAndCulture),
                makeCategory(t("TOTE", "Немецкий язык"), ids.german),
                makeCategory(t("TOTE", "Педагогика"), ids.pedagogy),
                makeCategory(t("TOTE", "Политология"), ids.politicalScience),
                makeCategory(t("TOTE", "Психология"), ids.psychology),
                makeCategory(t("TOTE", "Религиоведение, религия и мифология"), ids.religion),
                makeCategory(t("TOTE", "Риторика"), ids.rhetoric),
                makeCategory(t("TOTE", "Русский язык"), ids.russian),
                makeCategory(t("TOTE", "Социальная работа"), ids.socialWork),
                makeCategory(t("TOTE", "Социология"), ids.sociology),
                makeCategory(t("TOTE", "Стилистика"), ids.stylistics),
                makeCategory(t("TOTE", "Украинский язык"), ids.ukrainian),
                makeCategory(t("TOTE", "Физкультура и спорт"), ids.sportsAndFucking),
                makeCategory(t("TOTE", "Филология"), ids.philology),
                makeCategory(t("TOTE", "Философия"), ids.philosophy),
                makeCategory(t("TOTE", "Фонетика"), ids.phonetics),
                makeCategory(t("TOTE", "Французский язык"), ids.french)
            ))
        ))
        root.imposedIDToGenerate = ids.root
        saveCategoryTree(root)
    }


    private fun saveCategoryTree(root: UADocumentCategory) {
        val savedRoot = backPlatform.uaDocumentCategoryRepo.save(root)
        for (child in root.category.children) {
            child.category.parent = savedRoot
            saveCategoryTree(child)
        }
    }
}

@XComponent @XScope(XBeanDefinition.SCOPE_PROTOTYPE)
annotation class Servant



























