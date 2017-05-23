package vgrechka.spew

import vgrechka.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object DBCodeGenUtils {

    interface GeneratedBackingEntityProvider<out GeneratedClass> {
        val _backing: GeneratedClass
    }

    interface GeneratedEntity<out ManuallyDefinedInterface> {
        fun toManuallyDefinedInterface(): ManuallyDefinedInterface
    }

    class FuckingList<ElementManuallyDefinedInterface, ElementGeneratedClass : GeneratedEntity<ElementManuallyDefinedInterface>>(val getBackingList: () -> MutableList<ElementGeneratedClass>) : ReadWriteProperty<Any, MutableList<ElementManuallyDefinedInterface>> {
        override fun getValue(thisRef: Any, property: KProperty<*>): MutableList<ElementManuallyDefinedInterface> {
            val backingList = getBackingList()
            return object:MutableList<ElementManuallyDefinedInterface> {
                @Suppress("UNCHECKED_CAST")
                private val ElementManuallyDefinedInterface._backing
                    get() = (this as GeneratedBackingEntityProvider<ElementGeneratedClass>)._backing

                override val size: Int
                    get() = backingList.size

                override fun contains(element: ElementManuallyDefinedInterface): Boolean {
                    return backingList.contains(element._backing)
                }

                override fun containsAll(elements: Collection<ElementManuallyDefinedInterface>): Boolean {
                    return backingList.containsAll(elements.map {it._backing})
                }

                override fun get(index: Int): ElementManuallyDefinedInterface {
                    return backingList[index].toManuallyDefinedInterface()
                }

                override fun indexOf(element: ElementManuallyDefinedInterface): Int {
                    return backingList.indexOf(element._backing)
                }

                override fun isEmpty(): Boolean {
                    return backingList.isEmpty()
                }

                override fun iterator(): MutableIterator<ElementManuallyDefinedInterface> {
                    return backingIteratorToIterator(backingList.iterator())
                }

                private fun backingIteratorToIterator(backingIterator: MutableIterator<ElementGeneratedClass>): MutableIterator<ElementManuallyDefinedInterface> {
                    return object : MutableIterator<ElementManuallyDefinedInterface> {
                        override fun remove() {
                            backingIterator.remove()
                        }

                        override fun hasNext(): Boolean {
                            return backingIterator.hasNext()
                        }

                        override fun next(): ElementManuallyDefinedInterface {
                            return backingIterator.next().toManuallyDefinedInterface()
                        }
                    }
                }

                override fun lastIndexOf(element: ElementManuallyDefinedInterface): Int {
                    return backingList.lastIndexOf(element._backing)
                }

                override fun add(element: ElementManuallyDefinedInterface): Boolean {
                    return backingList.add(element._backing)
                }

                override fun add(index: Int, element: ElementManuallyDefinedInterface) {
                    return backingList.add(index, element._backing)
                }

                override fun addAll(index: Int, elements: Collection<ElementManuallyDefinedInterface>): Boolean {
                    return backingList.addAll(index, elements.map {it._backing})
                }

                override fun addAll(elements: Collection<ElementManuallyDefinedInterface>): Boolean {
                    return backingList.addAll(elements.map {it._backing})
                }

                override fun clear() {
                    return backingList.clear()
                }

                override fun listIterator(): MutableListIterator<ElementManuallyDefinedInterface> {
                    return backingListIteratorToListIterator(backingList.listIterator())
                }

                private fun backingListIteratorToListIterator(backingListIterator: MutableListIterator<ElementGeneratedClass>): MutableListIterator<ElementManuallyDefinedInterface> {
                    return object : MutableListIterator<ElementManuallyDefinedInterface> {
                        override fun add(element: ElementManuallyDefinedInterface) {
                            return backingListIterator.add(element._backing)
                        }

                        override fun hasNext(): Boolean {
                            return backingListIterator.hasNext()
                        }

                        override fun next(): ElementManuallyDefinedInterface {
                            return backingListIterator.next().toManuallyDefinedInterface()
                        }

                        override fun remove() {
                            return backingListIterator.remove()
                        }

                        override fun set(element: ElementManuallyDefinedInterface) {
                            return backingListIterator.set(element._backing)
                        }

                        override fun hasPrevious(): Boolean {
                            return backingListIterator.hasPrevious()
                        }

                        override fun nextIndex(): Int {
                            return backingListIterator.nextIndex()
                        }

                        override fun previous(): ElementManuallyDefinedInterface {
                            return backingListIterator.previous().toManuallyDefinedInterface()
                        }

                        override fun previousIndex(): Int {
                            return backingListIterator.previousIndex()
                        }
                    }
                }

                override fun listIterator(index: Int): MutableListIterator<ElementManuallyDefinedInterface> {
                    return backingListIteratorToListIterator(backingList.listIterator(index))
                }

                override fun remove(element: ElementManuallyDefinedInterface): Boolean {
                    return backingList.remove(element._backing)
                }

                override fun removeAll(elements: Collection<ElementManuallyDefinedInterface>): Boolean {
                    return backingList.removeAll(elements.map {it._backing})
                }

                override fun removeAt(index: Int): ElementManuallyDefinedInterface {
                    return backingList.removeAt(index).toManuallyDefinedInterface()
                }

                override fun retainAll(elements: Collection<ElementManuallyDefinedInterface>): Boolean {
                    return backingList.retainAll(elements.map {it._backing})
                }

                override fun set(index: Int, element: ElementManuallyDefinedInterface): ElementManuallyDefinedInterface {
                    return backingList.set(index, element._backing).toManuallyDefinedInterface()
                }

                override fun subList(fromIndex: Int, toIndex: Int): MutableList<ElementManuallyDefinedInterface> {
                    imf("a7db4be2-40bc-4cc2-9541-0a8ba80eac43")
                }
            }
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: MutableList<ElementManuallyDefinedInterface>) {
            imf("ca6c7f7a-8da5-4969-bbe8-eea87005a5e7")
        }
    }

    fun currentTimestampForEntity(): XTimestamp {
        return when {
//        backPlatform.isRequestThread() -> backPlatform.requestGlobus.stamp
            else -> XTimestamp(sharedPlatform.currentTimeMillis())
        }
    }
}


