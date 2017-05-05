package vgrechka

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.context.ApplicationContext
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import javax.persistence.*
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

typealias XSerializable = java.io.Serializable
typealias XTransient = Transient
typealias XCrudRepository<T, ID> = org.springframework.data.repository.CrudRepository<T, ID>
typealias XGenericGenerator = org.hibernate.annotations.GenericGenerator
typealias XTimestamp = java.sql.Timestamp
typealias XFetchType = FetchType
typealias XCascadeType = CascadeType
typealias XIndex = Index
typealias XEmbeddable = Embeddable
typealias XPreUpdate = PreUpdate
typealias XGenerationType = GenerationType
typealias XGeneratedValue = GeneratedValue
typealias XId = Id
typealias XMappedSuperclass = MappedSuperclass
typealias XEntity = Entity
typealias XTable = Table
typealias XEmbedded = Embedded
typealias XOneToMany = OneToMany
typealias XColumn = Column
typealias XOrderColumn = OrderColumn
typealias XEnumerated = Enumerated
typealias XEnumType = EnumType
typealias XManyToOne = ManyToOne
typealias XJsonIgnoreProperties = JsonIgnoreProperties
typealias XLogger = org.slf4j.Logger
typealias XDate = java.util.Date
typealias XXmlRootElement = XmlRootElement
typealias XXmlAccessorType = XmlAccessorType
typealias XXmlAccessType = XmlAccessType
typealias XXmlElement = XmlElement
typealias XCollections = Collections
typealias XBeanDefinition = org.springframework.beans.factory.config.BeanDefinition
typealias XScope = org.springframework.context.annotation.Scope
typealias XComponent =  org.springframework.stereotype.Component
typealias XDataSource = com.zaxxer.hikari.HikariDataSource
typealias XServletException = javax.servlet.ServletException
typealias XHttpServletRequest = javax.servlet.http.HttpServletRequest
typealias XHttpServletResponse = javax.servlet.http.HttpServletResponse
typealias XThreadLocal<T> = ThreadLocal<T>

object JVMBackPlatform : XBackPlatform {
    override var springctx by notNullOnce<ApplicationContext>()

    override fun <T> tx(block: (TransactionStatus) -> T): T {
        return TransactionTemplate(springctx.getBean(PlatformTransactionManager::class.java)).execute {
            block(it)
        }
    }
}

val backPlatform = JVMBackPlatform












