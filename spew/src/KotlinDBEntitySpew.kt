package vgrechka.spew

import org.jetbrains.kotlin.psi.*
import vgrechka.*
import vgrechka.BigPile.mangleUUID

class KotlinDBEntitySpew : Spew {
    override fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults) {
        CommonDBEntitySpew(
            ktFile, outputFilePath, spewResults,
            makePedro = {pedroCtx->
                val out = pedroCtx.out
                val ln = pedroCtx.out::appendln

                object : CommonDBEntitySpew.Pedro {
                    override fun mappedByCode(type: String, mappedBy: String): String {
                        return type.decapitalize() + "." + mappedBy
                    }

                    override fun generatedFinderName(entityName: String, shit: String, operator: String): String {
                        return "findBy${entityName}_$shit$operator"
                    }

                    override fun createTableSuffix(): String {
                        return ""
                    }

                    override fun spitImports() {
                        out.appendln("import vgrechka.db.*")
                    }

                    override fun makeJuan(juanCtx: CommonDBEntitySpew.spitShitForEntity): CommonDBEntitySpew.Juan {
                        val en = juanCtx.en
                        val end = juanCtx.end
                        val entity = juanCtx.entity

                        return object : CommonDBEntitySpew.Juan {
                            override fun createForeignKeyIndexDDL(field: FieldSpec, g: CommonDBEntitySpew.spitShitForEntity.generateDDLForEntity): String {
                                return "create index on ${g.quote(entity.tableName)} (${end}_${field.name}__id);\n"
                            }

                            override fun foreignKeyDDL(field: FieldSpec, oneEntity: EntitySpec): String {
                                return "    foreign key (${end}_${field.name}__id) references ${oneEntity.tableName}(id)"
                            }

                            override fun columnDDL(field: FieldSpec, sqlType: String): String {
                                return "${end}_${field.name}${(field.kind is FieldKind.One).thenElseEmpty{"__id"}} $sqlType"
                            }

                            override fun spitDDLForSpecialColumns(buf: StringBuilder) {
                                fun ln(s: String) = buf.append(s + "\n")

                                val idColumnDefinition = when (pedroCtx.databaseDialect) {
                                    GDBEntitySpewDatabaseDialect.SQLITE -> "id integer primary key autoincrement"
                                    GDBEntitySpewDatabaseDialect.POSTGRESQL -> "id bigserial primary key"
                                    GDBEntitySpewDatabaseDialect.MYSQL -> "id bigint not null auto_increment primary key"
                                }

                                ln("    $idColumnDefinition,")
                                ln("    ${end}_common_createdAt ${juanCtx.timestampType} not null,")
                                ln("    ${end}_common_updatedAt ${juanCtx.timestampType} not null,")
                                ln("    ${end}_common_deleted ${juanCtx.booleanType} not null,")

                            }

                            override fun spitRepo() {
                                out.append("val ${end}Repo: ${en}Repository by lazy {\n")
                                out.append("    val generatedRepo = backPlatform.springctx.getBean(Generated_${en}Repository::class.java)!!\n")
                                out.append("\n")
                                out.append("    object:${en}Repository {\n")
                                out.append("        override fun findOne(id: Long): $en? {\n")
                                out.append("            val shit = generatedRepo.findOne(id)\n")
                                out.append("            return shit?.toManuallyDefinedInterface()\n")
                                out.append("        }\n")
                                out.append("\n")
                                out.append("        override fun findAll(): List<$en> {\n")
                                out.append("            val shit = generatedRepo.findAll()\n")
                                out.append("            return shit.map {it.toManuallyDefinedInterface()}\n")
                                out.append("        }\n")
                                out.append("\n")
                                out.append("        override fun save(x: $en): $en {\n")
                                out.append("            val shit = generatedRepo.save(x._backing)\n")
                                out.append("            return shit.toManuallyDefinedInterface()\n")
                                out.append("        }\n")
                                out.append("\n")
                                out.append("        override fun delete(id: Long) {\n")
                                out.append("            generatedRepo.delete(id)\n")
                                out.append("        }\n")
                                out.append("\n")
                                out.append("        override fun delete(x : $en) {\n")
                                out.append("            generatedRepo.delete(x._backing)\n")
                                out.append("        }\n")
                                out.append("\n")

                                for ((finderIndex, finder) in entity.finders.withIndex()) {
                                    val paramsCode = StringBuilder()
                                    val generatedFinderArgsCode = StringBuilder()
                                    for ((paramIndex, param) in finder.params.withIndex()) {
                                        paramsCode += "${param.name}: ${param.type}"
                                        generatedFinderArgsCode += param.name
                                        if (paramIndex < finder.params.lastIndex) {
                                            paramsCode += ", "
                                            generatedFinderArgsCode += ", "
                                        }
                                    }
                                    val returnTypeCode = when {
                                        finder.returnsList -> "List<$en>"
                                        else -> en
                                    }

                                    out.append("        override fun ${finder.definedFinderName}($paramsCode): $returnTypeCode${CommonDBEntitySpew.maybeQuestion(finder)} {\n")
                                    out.append("            val shit = generatedRepo.${finder.generatedFinderName}($generatedFinderArgsCode)\n")
                                    if (finder.returnsList) {
                                        out.append("            return shit${CommonDBEntitySpew.maybeQuestion(finder)}.map {it.toManuallyDefinedInterface()}\n")
                                    } else {
                                        out.append("            return shit${CommonDBEntitySpew.maybeQuestion(finder)}.toManuallyDefinedInterface()\n")
                                    }
                                    out.append("        }\n")
                                    if (finderIndex < entity.finders.lastIndex)
                                        out.append("\n")
                                }

                                out.append("    }\n")
                                out.append("}\n\n")
                            }

                            override fun spitVariousShit() {
                                spitGeneratedRepo()

                                out.append("val $en._backing\n")
                                out.append("    get() = (this as Generated_${en}BackingProvider)._backing\n\n")

                                out.append("interface Generated_${en}BackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_$en> {\n")
                                out.append("    override val _backing: Generated_$en\n")
                                out.append("}\n\n")
                            }

                            private fun spitGeneratedRepo() {
                                out.append("interface Generated_${en}Repository : XCrudRepository<Generated_$en, Long> {\n")
                                for (finder in entity.finders) {
                                    if (finder.definedFinderName in setOf("findAll")) continue

                                    val paramsCode = StringBuilder()
                                    for ((paramIndex, param) in finder.params.withIndex()) {
                                        paramsCode += "${param.name}: ${param.type}"
                                        if (paramIndex < finder.params.lastIndex) {
                                            paramsCode += ", "
                                        }
                                    }
                                    val returnTypeCode = when {
                                        finder.returnsList -> "List<Generated_$en>"
                                        else -> "Generated_$en${CommonDBEntitySpew.maybeQuestion(finder)}"
                                    }
                                    out.append("    fun ${finder.generatedFinderName}($paramsCode): $returnTypeCode\n")
                                }
                                out.append("}\n\n")
                            }

                            override fun spitEntityClass() {
                                out.append("@XEntity @XTable(name = \"${entity.tableName}\")\n")
                                out.append("class Generated_$en( // Generated at ${mangleUUID("f21265f2-3d69-4ab8-a07c-5595106a9e6b")}\n")
                                out.append("    @XEmbedded var $end: Generated_${en}Fields\n")
                                out.append(")\n")
                                out.append("    : ClitoralEntity0(), DBCodeGenUtils.GeneratedEntity<$en>\n")
                                out.append("{\n")
                                out.append("    override fun toManuallyDefinedInterface(): $en {\n")
                                out.append("        return object : $en, Generated_${en}BackingProvider {\n")
                                out.append("            override val _backing: Generated_$en\n")
                                out.append("                get() = this@Generated_$en\n")
                                out.append("\n")
                                out.append("            override var id: Long\n")
                                out.append("                get() = _backing.id!!\n")
                                out.append("                set(value) {_backing.id = value}\n")
                                out.append("\n")
                                out.append("            override var createdAt: XTimestamp\n")
                                out.append("                get() = _backing.$end.common.createdAt\n")
                                out.append("                set(value) {_backing.$end.common.createdAt = value}\n")
                                out.append("\n")
                                out.append("            override var updatedAt: XTimestamp\n")
                                out.append("                get() = _backing.$end.common.updatedAt\n")
                                out.append("                set(value) {_backing.$end.common.updatedAt = value}\n")
                                out.append("\n")
                                out.append("            override var deleted: Boolean\n")
                                out.append("                get() = _backing.$end.common.deleted\n")
                                out.append("                set(value) {_backing.$end.common.deleted = value}\n")
                                out.append("\n")

                                for (field in entity.fields) {
                                    exhaustive=when (field.kind) {
                                        is FieldKind.Simple -> {
                                            out.append("            override var ${field.name}: ${field.type}\n")
                                            out.append("                get() = ${CommonDBEntitySpew.maybeUnwrapShitFromPlatformType(field, "_backing.$end.${field.name}")}\n")
                                            out.append("                set(value) {_backing.$end.${field.name} = ${CommonDBEntitySpew.maybeWrapShitIntoPlatformType(field, "value")}}\n")
                                            out.append("\n")
                                        }
                                        is FieldKind.One -> {
                                            out.append("            override var ${field.name}: ${field.type}\n")
                                            out.append("                get() = _backing.$end.${field.name}.toManuallyDefinedInterface()\n")
                                            out.append("                set(value) {_backing.$end.${field.name} = value._backing}\n")
                                            out.append("\n")
                                        }
                                        is FieldKind.Many -> {
                                            out.append("            override var ${field.name}: MutableList<${field.type}>\n")
                                            out.append("                by DBCodeGenUtils.FuckingList(getBackingList = {_backing.$end.${field.name}})\n")
                                            out.append("\n")
                                        }
                                    }
                                }

                                out.append("            override fun toString() = _backing.toString()\n")
                                out.append("\n")
                                out.append("            override fun hashCode() = _backing.hashCode()\n")
                                out.append("\n")
                                out.append("            override fun equals(other: Any?): Boolean {\n")
                                out.append("                val otherShit = other as? Generated_${en}BackingProvider ?: return false\n")
                                out.append("                return _backing == otherShit._backing\n")
                                out.append("            }\n")
                                out.append("        }\n")
                                out.append("    }\n")
                                out.append("\n")
                                out.append("    override fun toString(): String {\n")


                                out.append("        return \"$en(")
                                val toStringFields = entity.fields.filter {it.isInToString}
                                for ((index, field) in toStringFields.withIndex()) {
                                    out.append("${field.name}=\${$end.${field.name}}")
                                    if (index < toStringFields.lastIndex)
                                        out.append(", ")
                                }
                                out.append(")\"\n")

                                out.append("    }\n")
                                out.append("}\n")
                                out.append("\n")

                                out.append("@XEmbeddable\n")
                                out.append("class Generated_${en}Fields { // Generated at ${mangleUUID("2e91acff-5613-4b14-b71e-5edee254d029")}\n")
                                out.append("    @XEmbedded var common: CommonFields = CommonFields()\n")
                                for ((index, field) in entity.fields.withIndex()) {
                                    exhaustive=when (field.kind) {
                                        is FieldKind.Simple -> {
                                            out.append("    @XColumn")
                                            if (field.type == "String")
                                                out.append("(columnDefinition = \"text\")")
                                            if (field.type.endsWith("?"))
                                                out.append(" var ${field.name}: ${field.type} = null")
                                            else
                                                out.append(" lateinit var ${field.name}: ${CommonDBEntitySpew.platformType(field)}")
                                        }
                                        is FieldKind.One -> {
                                            out.append("    @XManyToOne(fetch = XFetchType.${field.kind.fetchType.name}/*, cascade = arrayOf(XCascadeType.ALL)*/)")
                                            out.append(" lateinit var ${field.name}: Generated_${field.type}")
                                        }
                                        is FieldKind.Many -> {
                                            out.append("    @XOneToMany(fetch = XFetchType.${field.kind.fetchType.name}, mappedBy = \"${field.kind.mappedBy}\", cascade = arrayOf(XCascadeType.ALL), orphanRemoval = true)")
                                            val fuckingEntity = pedroCtx.entities.find {it.name == field.type} ?: wtf("68c50078-c088-476b-9a52-7c1657412fc3")
                                            val orderColumnField = fuckingEntity.fields.find {it.isOrderColumn}
                                            if (orderColumnField != null) {
                                                out.append(" @XOrderColumn(name = \"${field.type.decapitalize()}_${orderColumnField.name}\")")
                                            }
                                            out.append(" var ${field.name}: MutableList<Generated_${field.type}> = mutableListOf()")
                                        }
                                    }
//            if (index < entity.fields.lastIndex)
//                out.append(",")
                                    out.append("\n")
                                }
                                out.append("}\n")
                                out.append("\n")
                            }

                            override fun spitEntityCtor() {
                                out.append("// Generated at ${mangleUUID("7470173f-49ef-43cb-adf7-1c395f07518c")}\n")
                                val fuck10 = "fun new$en("
                                out.append(fuck10)
                                val ctorParams = entity.fields.filter {it.kind !is FieldKind.Many && !it.isOrderColumn}
                                for ((ctorParamIndex, ctorParam) in ctorParams.withIndex()) {
                                    out.append("${ctorParam.name}: ${ctorParam.type}")
                                    if (ctorParamIndex < ctorParams.lastIndex)
                                        out.append(",\n" + " ".repeat(fuck10.length))
                                }
                                out.append("): $en {\n")

                                out.append("    val backing = Generated_$en(\n")
                                val fuck20 = "        Generated_${en}Fields().also {"
                                out.append(fuck20)
                                for ((ctorParamIndex, ctorParam) in ctorParams.withIndex()) {
                                    out.append("it.${ctorParam.name} = ${CommonDBEntitySpew.maybeWrapShitIntoPlatformType(ctorParam, ctorParam.name)}")
                                    if (ctorParam.isEntity)
                                        out.append("._backing")
                                    if (ctorParamIndex < ctorParams.lastIndex)
                                        out.append("\n" + " ".repeat(fuck20.length))
                                }
                                out.append("})\n")
                                out.append("    return backing.toManuallyDefinedInterface()\n")
                                out.append("}\n\n")
                            }
                        }
                    }
            }})
    }
    private fun noise(x: Any?) {
        if (false) clog(x)
    }
}
