package alraune.shared

import vgrechka.*

object AlDocumentCategories {
    val miscID = "102"
    val humanitiesID = "226"
    val linguisticsID = "238"
    val technicalID = "174"
    val programmingID = "186"

    val t = AlXPile::t
    val root = Category("ROOT", "101", listOf(
        Category(t("TOTE", "Разное"), miscID),
        Category(t("TOTE", "Экономические"), "103", listOf(
            Category(t("TOTE", "Аудит"), "104"),
            Category(t("TOTE", "Банковское дело"), "105"),
            Category(t("TOTE", "Биржевое дело"), "106"),
            Category(t("TOTE", "Бухгалтерский учет"), "107"),
            Category(t("TOTE", "Бюджетная система"), "108"),
            Category(t("TOTE", "Валютное регулирование и контроль"), "109"),
            Category(t("TOTE", "Валютные отношения"), "110"),
            Category(t("TOTE", "Деньги и кредит"), "111"),
            Category(t("TOTE", "Государственная служба"), "112"),
            Category(t("TOTE", "Государственное управление"), "113"),
            Category(t("TOTE", "Государственные финансы"), "114"),
            Category(t("TOTE", "Делопроизводство, документоведение, документалистика"), "115"),
            Category(t("TOTE", "Эконометрика"), "116"),
            Category(t("TOTE", "Экономика"), "117"),
            Category(t("TOTE", "Экономика предприятий"), "118"),
            Category(t("TOTE", "Экономика труда и социально-трудовые отношения"), "119"),
            Category(t("TOTE", "Экономическая кибернетика"), "120"),
            Category(t("TOTE", "Экономический анализ"), "121"),
            Category(t("TOTE", "Электронная коммерция"), "122"),
            Category(t("TOTE", "Связи с общественностью, PR"), "123"),
            Category(t("TOTE", "Внешнеэкономическая деятельность, ВЭД"), "124"),
            Category(t("TOTE", "Инвестирование, инвестиционная деятельность"), "125"),
            Category(t("TOTE", "Инновационная деятельность"), "126"),
            Category(t("TOTE", "Инновационный менеджмент"), "127"),
            Category(t("TOTE", "Казначейское дело"), "128"),
            Category(t("TOTE", "Контроллинг"), "129"),
            Category(t("TOTE", "Лесное хозяйство"), "130"),
            Category(t("TOTE", "Логистика"), "131"),
            Category(t("TOTE", "Макроэкономика, государственное регулирование экономики"), "132"),
            Category(t("TOTE", "Маркетинг, рекламная деятельность"), "133"),
            Category(t("TOTE", "Менеджмент, управление персоналом"), "134"),
            Category(t("TOTE", "Таможенное дело"), "135"),
            Category(t("TOTE", "Международная экономика и международные экономические отношения"), "136"),
            Category(t("TOTE", "Микроэкономика"), "137"),
            Category(t("TOTE", "Моделирование экономики"), "138"),
            Category(t("TOTE", "Налогообложение, налоги, налоговая система"), "139"),
            Category(t("TOTE", "Предпринимательство"), "140"),
            Category(t("TOTE", "Политэкономия, экономическая теория, история экономических учений"), "141"),
            Category(t("TOTE", "Ресторанно-гостиничный бизнес, бытовое обслуживание"), "142"),
            Category(t("TOTE", "Рынок ценных бумаг"), "143"),
            Category(t("TOTE", "Размещение производительных сил, региональная экономика, экономическая география, РПС"), "144"),
            Category(t("TOTE", "Сельское хозяйство и агропромышленный комплекс"), "145"),
            Category(t("TOTE", "Стандартизация, управление качеством"), "146"),
            Category(t("TOTE", "Статистика"), "147"),
            Category(t("TOTE", "Стратегический менеджмент"), "148"),
            Category(t("TOTE", "Страхование, страховое дело"), "149"),
            Category(t("TOTE", "Товароведение и экспертиза"), "150"),
            Category(t("TOTE", "Торговля и коммерческая деятельность"), "151"),
            Category(t("TOTE", "Туризм"), "152"),
            Category(t("TOTE", "Управление проектами"), "153"),
            Category(t("TOTE", "Управленческий учет"), "154"),
            Category(t("TOTE", "Финансы"), "155"),
            Category(t("TOTE", "Финансы предприятий"), "156"),
            Category(t("TOTE", "Финансовый анализ"), "157"),
            Category(t("TOTE", "Финансовый менеджмент"), "158"),
            Category(t("TOTE", "Ценообразование"), "159")
        )),

        Category(t("TOTE", "Естественные"), "160", listOf(
            Category(t("TOTE", "Астрономия"), "161"),
            Category(t("TOTE", "Биология"), "162"),
            Category(t("TOTE", "Военная подготовка"), "163"),
            Category(t("TOTE", "География"), "164"),
            Category(t("TOTE", "Геодезия"), "165"),
            Category(t("TOTE", "Геология"), "166"),
            Category(t("TOTE", "Экология"), "167"),
            Category(t("TOTE", "Математика"), "168"),
            Category(t("TOTE", "Медицина"), "169"),
            Category(t("TOTE", "Естествознание"), "170"),
            Category(t("TOTE", "Фармацевтика"), "171"),
            Category(t("TOTE", "Физика"), "172"),
            Category(t("TOTE", "Химия"), "173")
        )),

        Category(t("TOTE", "Технические"), technicalID, listOf(
            Category(t("TOTE", "Авиация и космонавтика"), "175"),
            Category(t("TOTE", "Архитектура"), "176"),
            Category(t("TOTE", "Базы данных"), "177"),
            Category(t("TOTE", "Строительство"), "178"),
            Category(t("TOTE", "Электроника"), "179"),
            Category(t("TOTE", "Электротехника"), "180"),
            Category(t("TOTE", "Информатика и вычислительная техника"), "181"),
            Category(t("TOTE", "Информационная безопасность"), "182"),
            Category(t("TOTE", "Информационно-аналитическая деятельность"), "183"),
            Category(t("TOTE", "Кибернетика"), "184"),
            Category(t("TOTE", "Чертежи"), "185"),
            Category(t("TOTE", "Программирование"), programmingID),
            Category(t("TOTE", "Проектирование"), "187"),
            Category(t("TOTE", "Радиоэлектроника, радиотехника"), "188"),
            Category(t("TOTE", "Теоретическая механика"), "189"),
            Category(t("TOTE", "Теория механизмов и машин (ТММ), детали машин (ДМ)"), "190"),
            Category(t("TOTE", "Теплотехника"), "191"),
            Category(t("TOTE", "Технологии, система технологий"), "192"),
            Category(t("TOTE", "Технология машиностроения"), "193"),
            Category(t("TOTE", "Технология приготовления пищи"), "194"),
            Category(t("TOTE", "Транспортное строительство"), "195")
        )),

        Category(t("TOTE", "Юридические"), "196", listOf(
            Category(t("TOTE", "Адвокатура"), "197"),
            Category(t("TOTE", "Административное право"), "198"),
            Category(t("TOTE", "Арбитражный процесс"), "199"),
            Category(t("TOTE", "Хозяйственное право"), "200"),
            Category(t("TOTE", "Экологическое право"), "201"),
            Category(t("TOTE", "Жилищное право"), "202"),
            Category(t("TOTE", "Земельное право"), "203"),
            Category(t("TOTE", "История государства и права"), "204"),
            Category(t("TOTE", "Конституционное право"), "205"),
            Category(t("TOTE", "Корпоративное право"), "206"),
            Category(t("TOTE", "Криминалистика, экспертиза"), "207"),
            Category(t("TOTE", "Уголовное право, криминология"), "208"),
            Category(t("TOTE", "Уголовный процесс"), "209"),
            Category(t("TOTE", "Таможенное право"), "210"),
            Category(t("TOTE", "Международное право"), "211"),
            Category(t("TOTE", "Муниципальное право"), "212"),
            Category(t("TOTE", "Нотариат"), "213"),
            Category(t("TOTE", "Предпринимательское право"), "214"),
            Category(t("TOTE", "Налоговое право"), "215"),
            Category(t("TOTE", "Право"), "216"),
            Category(t("TOTE", "Право интеллектуальной собственности"), "217"),
            Category(t("TOTE", "Семейное право"), "218"),
            Category(t("TOTE", "Страховое право"), "219"),
            Category(t("TOTE", "Судебные и правоохранительные органы"), "220"),
            Category(t("TOTE", "Судебно-медицинская экспертиза"), "221"),
            Category(t("TOTE", "Теория государства и права"), "222"),
            Category(t("TOTE", "Трудовое право"), "223"),
            Category(t("TOTE", "Финансовое право"), "224"),
            Category(t("TOTE", "Гражданское право"), "225")
        )),

        Category(t("TOTE", "Гуманитарные"), humanitiesID, listOf(
            Category(t("TOTE", "Анализ банковской деятельности"), "227"),
            Category(t("TOTE", "Английский язык"), "228"),
            Category(t("TOTE", "Безопасность жизнедеятельности (БЖД)"), "229"),
            Category(t("TOTE", "Дизайн"), "230"),
            Category(t("TOTE", "Дипломатия"), "231"),
            Category(t("TOTE", "Эстетика"), "232"),
            Category(t("TOTE", "Этика"), "233"),
            Category(t("TOTE", "Журналистика и издательское дело"), "234"),
            Category(t("TOTE", "История"), "235"),
            Category(t("TOTE", "Краеведение"), "236"),
            Category(t("TOTE", "Культурология"), "237"),
            Category(t("TOTE", "Лингвистика"), linguisticsID),
            Category(t("TOTE", "Литература зарубежная"), "239"),
            Category(t("TOTE", "Литература русский"), "240"),
            Category(t("TOTE", "Литература украинский"), "241"),
            Category(t("TOTE", "Логика"), "242"),
            Category(t("TOTE", "Искусство и культура"), "243"),
            Category(t("TOTE", "Немецкий язык"), "244"),
            Category(t("TOTE", "Педагогика"), "245"),
            Category(t("TOTE", "Политология"), "246"),
            Category(t("TOTE", "Психология"), "247"),
            Category(t("TOTE", "Религиоведение, религия и мифология"), "248"),
            Category(t("TOTE", "Риторика"), "249"),
            Category(t("TOTE", "Русский язык"), "250"),
            Category(t("TOTE", "Социальная работа"), "251"),
            Category(t("TOTE", "Социология"), "252"),
            Category(t("TOTE", "Стилистика"), "253"),
            Category(t("TOTE", "Украинский язык"), "254"),
            Category(t("TOTE", "Физкультура и спорт"), "255"),
            Category(t("TOTE", "Филология"), "256"),
            Category(t("TOTE", "Философия"), "257"),
            Category(t("TOTE", "Фонетика"), "258"),
            Category(t("TOTE", "Французский язык"), "259")
        ))
    ))

    class Category(val title: String, val id: String, val children: List<Category> = listOf()) {
        var parent: Category? = null

        init {
            for (child in children) {
                child.parent = this
            }
        }
    }

    fun findByIDOrBitch(id: String): Category {
        return maybeFindByID(id, root) ?: bitch("id = $id    c6606a3c-c677-4f8d-8a0b-b1336efbd3fb")
    }

    fun maybeFindByID(id: String, parent: Category): Category? {
        for (child in parent.children) {
            if (child.id == id)
                return child
            else
                maybeFindByID(id, child)?.let {
                    return it
                }
        }
        return null
    }
}












