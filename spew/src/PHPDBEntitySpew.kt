package vgrechka.spew

import org.jetbrains.kotlin.psi.KtFile
import vgrechka.*
import vgrechka.BigPile.mangleUUID
import java.util.*

class PHPDBEntitySpew : Spew {
    override fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults) {
        CommonDBEntitySpew(
            ktFile, outputFilePath, spewResults,
            pedro = object : CommonDBEntitySpew.Pedro {
                override fun spitRepo(ctx: CommonDBEntitySpew.spitShitForEntity, ctx2: CommonDBEntitySpew) {
                    val ln = ctx.out::appendln; val en = ctx.en; val end = ctx.end
                    fun ap(s: String) = ctx.out.append(s)

                    object {
                        init {
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
                            for ((index, field) in ctx.entity.fields.withIndex()) {
                                val shit = when {
                                    field.name == "id" -> "cast(id as char)"
                                    field.name == "createdAt" -> "unix_timestamp(${end}_common_createdAt)"
                                    field.name == "updatedAt" -> "unix_timestamp(${end}_common_updatedAt)"
                                    field.name == "deleted" -> "${end}_common_deleted"
                                    else -> "${end}_${field.name}"
                                }
                                ap("                ln(\"    $shit")
                                if (index < ctx.entity.fields.lastIndex)
                                    ap(",")
                                ap("\")")
                                ln("")
                            }
                            ln("                ln(\"from `${ctx.entity.tableName}`\")")
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
                            for ((index, field) in ctx.entity.fields.withIndex()) {
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
                            ln("                    ln(\"insert into `${ctx.entity.tableName}`(\")")
                            val fields = ctx.entity.fields.filter {it.name !in setOf("id")}
                            for (field in fields) {
                                if (field.name in setOf("createdAt", "updatedAt", "deleted"))
                                    ln("                    ln(\"    `${end}_common_${field.name}`,\")")
                                else
                                    ln("                    ln(\"    `${end}_${field.name}`,\")")
                            }
                            ctx.out.deleteLastCommaBeforeDoubleQuoteClosingParenAndNewLine()
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
                            ctx.out.deleteLastCommaBeforeNewLine()
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

                override fun spitVariousShit(ctx: CommonDBEntitySpew.spitShitForEntity, ctx2: CommonDBEntitySpew) {
                }

                override fun spitEntityClass(ctx: CommonDBEntitySpew.spitShitForEntity, ctx2: CommonDBEntitySpew) {
                    val ln = ctx.out::appendln; val en = ctx.en
                    fun ap(s: String) = ctx.out.append(s)

                    ln("class Generated_$en : $en {")
                    for (field in ctx.entity.fields) {
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
                    val fields = ctx.entity.fields.filter {field->
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

                override fun spitImports(ctx: CommonDBEntitySpew) {
                    val ln = ctx.out::appendln
                    ln("import kotlin.properties.Delegates.notNull")
                    ln("import kotlin.reflect.KMutableProperty1")
                }

                override fun spitEntityCtor(ctx: CommonDBEntitySpew.spitShitForEntity) {
                    val ln = ctx.out::appendln
                    val out = ctx.out; val en = ctx.en; val entity = ctx.entity

                    out.append("// Generated at ${mangleUUID("e68337cd-2d2d-4aa0-bfcc-e6423096599d")}\n")

                    ln("fun new$en(")
                    val skipFields = setOf("id", "createdAt", "updatedAt", "deleted")
                    for (field in ctx.entity.fields) {
                        if (field.name !in skipFields) {
                            ln("    ${field.name}: ${field.type},")
                        }
                    }
                    out.deleteLastCommaBeforeNewLine()
                    ln("): $en {")
                    ln("    return Generated_$en().also {")
                    for (field in ctx.entity.fields) {
                        if (field.name !in skipFields) {
                            ln("        it.${field.name} = ${field.name}")
                        }
                    }
                    ln("    }")
                    ln("}")
                }
            })
    }

    private fun noise(x: Any?) {
        if (false) clog(x)
    }
}

