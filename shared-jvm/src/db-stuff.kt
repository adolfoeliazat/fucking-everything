package vgrechka.db

import org.hibernate.boot.model.naming.Identifier
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl
import org.hibernate.boot.model.source.spi.AttributePath
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentityGenerator
import vgrechka.*

class NiceHibernateNamingStrategy : ImplicitNamingStrategyJpaCompliantImpl() {
    override fun transformAttributePath(attributePath: AttributePath): String {
        return attributePath.fullPath.replace(".", "_")
    }

    override fun determineJoinColumnName(source: ImplicitJoinColumnNameSource): Identifier {
        val name: String
        if (source.nature == ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION || source.attributePath == null ) {
            name = transformEntityName(source.entityNaming) + "__" + source.referencedColumnName.text
        } else {
            name = transformAttributePath(source.attributePath) + "__" + source.referencedColumnName.text
        }
        return toIdentifier(name, source.buildingContext)
    }
}

@XMappedSuperclass
abstract class ClitoralEntity0 {
    @XId
    @XGeneratedValue(strategy = XGenerationType.IDENTITY, generator = "IdentityIfNotSetGenerator")
    @XGenericGenerator(name = "IdentityIfNotSetGenerator", strategy = "vgrechka.db.IdentityIfNotSetGenerator")
    var id: Long? = null

    @XTransient
    var imposedIDToGenerate: Long? = null

    @XPreUpdate
    fun preFuckingUpdate() {
//        if (backPlatform.isRequestThread() && !backPlatform.requestGlobus.shitIsDangerous) {
//            if (this is User) {
//                saveUserParamsHistory(this)
//            }
//        }
    }
}

@Suppress("Unused")
class IdentityIfNotSetGenerator : IdentityGenerator() {
    private val logic = IdentityIfNotSetGeneratorLogic()

    override fun generate(s: SharedSessionContractImplementor?, obj: Any?): XSerializable {
        val id = logic.generate(obj)
        return when {
            id != null -> id
            else -> super.generate(s, obj)
        }
    }
}

// TODO:vgrechka Why the fuck did I need this to be in a separate class?
class IdentityIfNotSetGeneratorLogic {
    /**
     * @return null if default identity generator should be used
     */
    fun generate(obj: Any?): Long? {
        val entity = obj as ClitoralEntity0
        val id = entity.id
        val imposedIDToGenerate = entity.imposedIDToGenerate
        return when {
            id != null -> id
            imposedIDToGenerate != null -> imposedIDToGenerate
            else -> null
        }
    }
}




