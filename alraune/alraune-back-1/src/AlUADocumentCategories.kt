package alraune.shared

import vgrechka.*

object AlUADocumentCategories {
    val miscID = "102"
    val humanitiesID = "226"
    val linguisticsID = "238"
    val technicalID = "174"
    val programmingID = "186"

    val root = Category("ROOT", "101", listOf(
        Category("Разное", miscID),
        Category("Экономические", "103", listOf(
            Category("Аудит", "104"),
            Category("Банковское дело", "105"),
            Category("Биржевое дело", "106"),
            Category("Бухгалтерский учет", "107"),
            Category("Бюджетная система", "108"),
            Category("Валютное регулирование и контроль", "109"),
            Category("Валютные отношения", "110"),
            Category("Деньги и кредит", "111"),
            Category("Государственная служба", "112"),
            Category("Государственное управление", "113"),
            Category("Государственные финансы", "114"),
            Category("Делопроизводство, документоведение, документалистика", "115"),
            Category("Эконометрика", "116"),
            Category("Экономика", "117"),
            Category("Экономика предприятий", "118"),
            Category("Экономика труда и социально-трудовые отношения", "119"),
            Category("Экономическая кибернетика", "120"),
            Category("Экономический анализ", "121"),
            Category("Электронная коммерция", "122"),
            Category("Связи с общественностью, PR", "123"),
            Category("Внешнеэкономическая деятельность, ВЭД", "124"),
            Category("Инвестирование, инвестиционная деятельность", "125"),
            Category("Инновационная деятельность", "126"),
            Category("Инновационный менеджмент", "127"),
            Category("Казначейское дело", "128"),
            Category("Контроллинг", "129"),
            Category("Лесное хозяйство", "130"),
            Category("Логистика", "131"),
            Category("Макроэкономика, государственное регулирование экономики", "132"),
            Category("Маркетинг, рекламная деятельность", "133"),
            Category("Менеджмент, управление персоналом", "134"),
            Category("Таможенное дело", "135"),
            Category("Международная экономика и международные экономические отношения", "136"),
            Category("Микроэкономика", "137"),
            Category("Моделирование экономики", "138"),
            Category("Налогообложение, налоги, налоговая система", "139"),
            Category("Предпринимательство", "140"),
            Category("Политэкономия, экономическая теория, история экономических учений", "141"),
            Category("Ресторанно-гостиничный бизнес, бытовое обслуживание", "142"),
            Category("Рынок ценных бумаг", "143"),
            Category("Размещение производительных сил, региональная экономика, экономическая география, РПС", "144"),
            Category("Сельское хозяйство и агропромышленный комплекс", "145"),
            Category("Стандартизация, управление качеством", "146"),
            Category("Статистика", "147"),
            Category("Стратегический менеджмент", "148"),
            Category("Страхование, страховое дело", "149"),
            Category("Товароведение и экспертиза", "150"),
            Category("Торговля и коммерческая деятельность", "151"),
            Category("Туризм", "152"),
            Category("Управление проектами", "153"),
            Category("Управленческий учет", "154"),
            Category("Финансы", "155"),
            Category("Финансы предприятий", "156"),
            Category("Финансовый анализ", "157"),
            Category("Финансовый менеджмент", "158"),
            Category("Ценообразование", "159")
        )),

        Category("Естественные", "160", listOf(
            Category("Астрономия", "161"),
            Category("Биология", "162"),
            Category("Военная подготовка", "163"),
            Category("География", "164"),
            Category("Геодезия", "165"),
            Category("Геология", "166"),
            Category("Экология", "167"),
            Category("Математика", "168"),
            Category("Медицина", "169"),
            Category("Естествознание", "170"),
            Category("Фармацевтика", "171"),
            Category("Физика", "172"),
            Category("Химия", "173")
        )),

        Category("Технические", technicalID, listOf(
            Category("Авиация и космонавтика", "175"),
            Category("Архитектура", "176"),
            Category("Базы данных", "177"),
            Category("Строительство", "178"),
            Category("Электроника", "179"),
            Category("Электротехника", "180"),
            Category("Информатика и вычислительная техника", "181"),
            Category("Информационная безопасность", "182"),
            Category("Информационно-аналитическая деятельность", "183"),
            Category("Кибернетика", "184"),
            Category("Чертежи", "185"),
            Category("Программирование", programmingID),
            Category("Проектирование", "187"),
            Category("Радиоэлектроника, радиотехника", "188"),
            Category("Теоретическая механика", "189"),
            Category("Теория механизмов и машин", "190"),
            Category("Теплотехника", "191"),
            Category("Технологии, система технологий", "192"),
            Category("Технология машиностроения", "193"),
            Category("Технология приготовления пищи", "194"),
            Category("Транспортное строительство", "195")
        )),

        Category("Юридические", "196", listOf(
            Category("Адвокатура", "197"),
            Category("Административное право", "198"),
            Category("Арбитражный процесс", "199"),
            Category("Хозяйственное право", "200"),
            Category("Экологическое право", "201"),
            Category("Жилищное право", "202"),
            Category("Земельное право", "203"),
            Category("История государства и права", "204"),
            Category("Конституционное право", "205"),
            Category("Корпоративное право", "206"),
            Category("Криминалистика, экспертиза", "207"),
            Category("Уголовное право, криминология", "208"),
            Category("Уголовный процесс", "209"),
            Category("Таможенное право", "210"),
            Category("Международное право", "211"),
            Category("Муниципальное право", "212"),
            Category("Нотариат", "213"),
            Category("Предпринимательское право", "214"),
            Category("Налоговое право", "215"),
            Category("Право", "216"),
            Category("Право интеллектуальной собственности", "217"),
            Category("Семейное право", "218"),
            Category("Страховое право", "219"),
            Category("Судебные и правоохранительные органы", "220"),
            Category("Судебно-медицинская экспертиза", "221"),
            Category("Теория государства и права", "222"),
            Category("Трудовое право", "223"),
            Category("Финансовое право", "224"),
            Category("Гражданское право", "225")
        )),

        Category("Гуманитарные", humanitiesID, listOf(
            Category("Анализ банковской деятельности", "227"),
            Category("Английский язык", "228"),
            Category("Безопасность жизнедеятельности (БЖД)", "229"),
            Category("Дизайн", "230"),
            Category("Дипломатия", "231"),
            Category("Эстетика", "232"),
            Category("Этика", "233"),
            Category("Журналистика и издательское дело", "234"),
            Category("История", "235"),
            Category("Краеведение", "236"),
            Category("Культурология", "237"),
            Category("Лингвистика", linguisticsID),
            Category("Литература зарубежная", "239"),
            Category("Литература русский", "240"),
            Category("Литература украинский", "241"),
            Category("Логика", "242"),
            Category("Искусство и культура", "243"),
            Category("Немецкий язык", "244"),
            Category("Педагогика", "245"),
            Category("Политология", "246"),
            Category("Психология", "247"),
            Category("Религиоведение, религия и мифология", "248"),
            Category("Риторика", "249"),
            Category("Русский язык", "250"),
            Category("Социальная работа", "251"),
            Category("Социология", "252"),
            Category("Стилистика", "253"),
            Category("Украинский язык", "254"),
            Category("Физкультура и спорт", "255"),
            Category("Филология", "256"),
            Category("Философия", "257"),
            Category("Фонетика", "258"),
            Category("Французский язык", "259")
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












