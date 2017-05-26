package vgrechka.spew

import org.jetbrains.kotlin.psi.KtFile
import vgrechka.*
import vgrechka.BigPile.mangleUUID
import java.util.*

class PHPDBEntitySpew : Spew {
    override fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults) {
        CommonDBEntitySpew(
            ktFile, outputFilePath, spewResults,
            makePedro = {pedroCtx->
                val ln = pedroCtx.out::appendln
                val out = pedroCtx.out

                object : CommonDBEntitySpew.Pedro {
                    override fun mappedByCode(type: String, mappedBy: String): String {
                        imf("31687638-ad1e-463f-b63c-54b32a6c63ec")
                    }

                    override fun generatedFinderName(entityName: String, shit: String, operator: String): String {
                        imf("ae5ed683-5110-48c4-98ec-a7079d46881b")
                    }

                    override fun createTableSuffix(): String {
                        return "engine=InnoDB"
                    }

                    override fun spitImports() {
                        ln("import kotlin.properties.Delegates.notNull")
                        ln("import kotlin.reflect.KMutableProperty1")
                    }

                    override fun makeJuan(juanCtx: CommonDBEntitySpew.spitShitForEntity): CommonDBEntitySpew.Juan {
                        val en = juanCtx.en
                        val end = juanCtx.end
                        val entity = juanCtx.entity
                        fun ap(s: String) = out.append(s)

                        return object : CommonDBEntitySpew.Juan {
                            override fun createForeignKeyIndexDDL(field: FieldSpec, g: CommonDBEntitySpew.spitShitForEntity.generateDDLForEntity): String {
                                imf("d625f258-6929-4558-b6b0-a5899a0516e2")
                            }

                            override fun foreignKeyDDL(field: FieldSpec, oneEntity: EntitySpec): String {
                                imf("5d9f1b66-209f-4217-9746-1d5e49b87d36")
                            }

                            override fun columnDDL(field: FieldSpec, sqlType: String): String {
                                imf("c7f5e133-8e9d-42d0-a3ab-f42b1f48d7e1")
                            }

                            override fun spitDDLForSpecialColumns(buf: StringBuilder) {
                                fun ln(s: String) = buf.append(s + "\n")

                                val idColumnName = "${end}_id"
                                val idColumnDefinition = when (pedroCtx.databaseDialect) {
                                    GDBEntitySpewDatabaseDialect.SQLITE -> "$idColumnName integer primary key autoincrement"
                                    GDBEntitySpewDatabaseDialect.POSTGRESQL -> "$idColumnName bigserial primary key"
                                    GDBEntitySpewDatabaseDialect.MYSQL -> "$idColumnName bigint not null auto_increment primary key"
                                }

                                ln("    $idColumnDefinition,")
                                ln("    ${end}_createdAt ${juanCtx.timestampType} not null,")
                                ln("    ${end}_updatedAt ${juanCtx.timestampType} not null,")
                                ln("    ${end}_deleted ${juanCtx.booleanType} not null,")
                            }

                            override fun spitVariousShit() {
                                ln("")
                                ln("fun ${en}Repository.propertyToColumnName(prop: KMutableProperty1<$en, String>): String {")
                                for (field in entity.fields) {
                                    ln("    if (prop.name == $en::${field.name}.name) return \"${end}_${field.name}\"")
                                }
                                ln("    throw Exception(\"${UUID.randomUUID()}\")")
                                ln("")
                                ln("}")
                                ln("")

                                ln("val ${en}Repository.dropTableDDL get() = buildString {")
                                ln("    ln(\"drop table if exists `${entity.tableName}`\")")
                                ln("}")

                                ln("val ${en}Repository.createTableDDL get() = buildString {")
                                out.append(CodeShitter(beforeReification = {
                                    val create = pedroCtx.creates.find {it.table == entity.tableName}!!
                                    for (line in create.ddl.lines()) {
                                        if (line.isNotBlank())
                                            it.appendln("    ln(\"$line\")")
                                    }
                                }))
                                ln("}")
                            }

                            override fun spitEntityClass() {
                                ln("class Generated_$en : $en {")
                                for (field in entity.fields) {
                                    ap("    override var ${field.name}")
                                    if (field.isNullable()) {
                                        ap(": ${field.type} = null")
                                    } else {
                                        when (field.name) {
                                            "createdAt" -> ap(": PHPTimestamp = DBPile.currentTimestampForEntity()")
                                            "updatedAt" -> ap("= createdAt")
                                            "deleted" -> ap("= false")
                                            else -> ap(" by notNull<${field.type}>()")
                                        }
                                    }
                                    ln("")
                                }

                                ln("")
                                ln("    override fun toString(): String {")
                                ln("        return buildString {")
                                ln("            append(\"$en(\")")
                                val fields = entity.fields.filter {field->
                                    val name = field.name.toLowerCase()
                                    val scaryWords = setOf("password", "secret", "token")
                                    !scaryWords.any {field.name.contains(it)}
                                }
                                for ((index, field) in fields.withIndex()) {
                                    val valueCode = when (field.type) {
                                        "PHPTimestamp" -> "\${phiEval(\"return gmdate('Y-m-d H:i:s', \${createdAt.time});\") as String}"
                                        else -> "\$${field.name}"
                                    }
                                    ap("            append(\"${field.name}=$valueCode")
                                    if (index < fields.lastIndex)
                                        ap(", ")
                                    ap("\")")
                                    ln("")
                                }
                                ln("            append(\")\")")
                                ln("        }")
                                ln("    }")

                                ln("}")
                                ln("")
                            }
                            override fun spitEntityCtor() {
                                out.append("// Generated at ${mangleUUID("e68337cd-2d2d-4aa0-bfcc-e6423096599d")}\n")

                                ln("fun new$en(")
                                val skipFields = setOf("id", "createdAt", "updatedAt", "deleted")
                                for (field in entity.fields) {
                                    if (field.name !in skipFields) {
                                        ln("    ${field.name}: ${field.type},")
                                    }
                                }
                                out.deleteLastCommaBeforeNewLine()
                                ln("): $en {")
                                ln("    return Generated_$en().also {")
                                for (field in entity.fields) {
                                    if (field.name !in skipFields) {
                                        ln("        it.${field.name} = ${field.name}")
                                    }
                                }
                                ln("    }")
                                ln("}")
                            }

                            override fun spitRepo() {
                                ln("")
                                ln("val ${end}Repo: ${en}Repository by lazy {")
                                ln("    object : ${en}Repository {")
                                spitSelect()
                                spitInsert()
                                ln("    }")
                                ln("}")
                            }

                            fun spitSelect() {
                                ln("        override fun select(prop: KMutableProperty1<$en, String>, op: DBPile.Operator, arg: Any?): List<$en> {")
                                ln("            println(\"findBy(prop = \${prop.name}; op = \${op.toString()}; arg = \$arg)\")")
                                ln("            val params = mutableListOf<Any?>()")
                                ln("            val sql = buildString {")
                                ln("                ln(\"select\")")
                                for ((index, field) in entity.fields.withIndex()) {
                                    val shit = when {
                                        field.name == "${end}_id" -> "cast(id as char)"
                                        field.name == "createdAt" -> "unix_timestamp(${end}_createdAt)"
                                        field.name == "updatedAt" -> "unix_timestamp(${end}_updatedAt)"
                                        field.name == "deleted" -> "${end}_deleted"
                                        else -> "${end}_${field.name}"
                                    }
                                    ap("                ln(\"    $shit")
                                    if (index < entity.fields.lastIndex)
                                        ap(",")
                                    ap("\")")
                                    ln("")
                                }
                                ln("                ln(\"from `${entity.tableName}`\")")
                                ln("                ln(\"where\")")
                                ln("                ln(\"\${propertyToColumnName(prop)} \${op.sql} ?\")")
                                ln("                params.add(arg)")

                                ln("            }")

                                ln("            val rows = DBPile.query(sql, params, uuid = \"${UUID.randomUUID()}\")")
                                ln("            println(\"findBy: Found \${rows.size} rows\")")
                                ln("            val items = mutableListOf<$en>()")
                                ln("            for (row in rows) {")
                                ln("//                run {")
                                ln("//                    val value = row[13]")
                                ln("//                    println(\"--- type = \${PHPPile.getType(value)}; value = \$value\")")
                                ln("//                }")
                                ln("                items += Generated_$en().also {")
                                for ((index, field) in entity.fields.withIndex()) {
                                    ap("                    it.${field.name} = ")
                                    when {
                                        field.name == "id" -> ln("row[$index] as String")
                                        field.type == "PHPTimestamp" -> ln("DBPile.mysqlValueToPHPTimestamp(row[$index])")
                                        field.type == "Boolean" -> ln("DBPile.mysqlValueToBoolean(row[$index])")
                                        field.typeWithoutQuestion() == "String" -> ln("row[$index] as ${field.type}")
                                        else -> wtf("field.name = ${field.name}    4d1a8e87-28f7-4e57-bdf4-2d70ddbbeb77")
                                    }
                                }
                                ln("                }")
                                ln("            }")
                                ln("            return items")
                                ln("        }")

                            }

                            fun spitInsert() {
                                ln("        override fun insert(x: $en): $en {")
                                ln("            DBPile.execute(")
                                ln("                sql = buildString {")
                                ln("                    ln(\"insert into `${entity.tableName}`(\")")
                                val fields = entity.fields.filter {it.name !in setOf("id")}
                                for (field in fields) {
                                    ln("                    ln(\"    `${end}_${field.name}`,\")")
                                }
                                out.deleteLastCommaBeforeDoubleQuoteClosingParenAndNewLine()
                                ln("                    ln(\") values (${"?".repeatToList(fields.size).joinToString(", ")})\")")
                                ln("                },")
                                ln("                params = listOf(")
                                for (field in fields) {
                                    when {
                                        field.type == "PHPTimestamp" ->
                                            ln("                    phiEval(\"return gmdate('Y-m-d H:i:s', \${x.${field.name}.time});\") as String,")
                                        else ->
                                            ln("                    x.${field.name},")
                                    }
                                }
                                out.deleteLastCommaBeforeNewLine()
                                ln("                ),")
                                ln("                uuid = \"${UUID.randomUUID()}\"")
                                ln("            )")
                                ln("")
                                ln("            val res = DBPile.query(")
                                ln("                sql = \"select cast(last_insert_id() as char)\",")
                                ln("                uuid = \"${UUID.randomUUID()}\"")
                                ln("            )")
                                ln("")
                                ln("            x.id = res.first().first() as String")
                                ln("            return x")
                                ln("        }")
                            }
                        }
                    }

                }
            }
        )
    }

    private fun noise(x: Any?) {
        if (false) clog(x)
    }
}

