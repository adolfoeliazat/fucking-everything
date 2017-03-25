package aps

import aps.back.*
import photlin.PHPDumpBodyToContainer
import photlin.assertEquals
import photlin.assertVarExportEquals
import photlin.count

val sharedPlatform = object : XSharedPlatform {
    override fun currentTimeMillis(): Long {
        imf("4edaf379-ad6a-4d11-a4d2-3fffb8372576")
    }

    override fun getenv(name: String): String? {
        imf("001706a0-c7a6-4fbf-b57f-f0069cfff08b")
    }
}

class Pipiska

class Pizda {
    class Clitoris
}

fun <T> listOf(vararg elements: T): List<T> = imf("4de6e2e0-e076-4570-b962-15f56f9d2895")
fun <T> mutableListOf(vararg elements: T): MutableList<T> = imf("0acf714b-f134-42fc-af6d-67fc37ecc357")

fun <T> setOf(vararg elements: T): Set<T> = mutableSetOf(*elements)

fun <T> mutableSetOf(vararg elements: T): MutableSet<T> {
    return object:MutableSet<T> {
        override fun add(element: T): Boolean {
            imf("71ba5a03-b3b9-4f12-9827-b8f44d541cdc")
        }

        override fun addAll(elements: Collection<T>): Boolean {
            imf("19f86d68-5fc3-4974-a303-51534bef0658")
        }

        override fun clear() {
            imf("dd2d6b7f-6389-4780-950a-fc036db38112")
        }

        override fun iterator(): MutableIterator<T> {
            imf("49097846-ce64-44ae-9532-fbbedeb43d66")
        }

        override fun remove(element: T): Boolean {
            imf("cb981823-6d23-4e95-b6fe-bc1a71db9e78")
        }

        override fun removeAll(elements: Collection<T>): Boolean {
            imf("2db96068-7f25-49f5-9527-9d5afb28c4ef")
        }

        override fun retainAll(elements: Collection<T>): Boolean {
            imf("81bf5195-a235-480d-a3c2-0344d92e7995")
        }

        override val size: Int
            get() {
                imf("9b6fe4d9-a03d-4429-9972-d7ecb5f5b08d")
            }

        override fun contains(element: T): Boolean {
            imf("c11d1bf4-5d23-4baa-bb34-d6da7a4be23e")
        }

        override fun containsAll(elements: Collection<T>): Boolean {
            imf("e0d72b61-f8f2-437f-b961-c77c9459d56b")
        }

        override fun isEmpty(): Boolean {
            imf("0c15d948-b154-444f-9a56-b92654402efd")
        }

    }
}

fun <K, V> mapOf(vararg pairs: Pair<K, V>): Map<K, V> = imf("124cca89-adc0-4bcd-9a5f-22bbea06c2be")
fun <K, V> mutableMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V> = imf("8d93f745-faa2-4963-8e72-6dd2d76bd70f")

inline fun <T, R> Iterable<T>.map(transform: (T) -> R): List<R> {
    imf("7692fed3-d51c-4de7-95d9-6b9f23019c29")
}


fun CharSequence.split(delim: String): List<String> {
    return explode(delim, this.toString()).toList()
}

@PHPDumpBodyToContainer
fun quickTest_CharSequence_split() {
    val list = "fuck:shit:bitch".split(":")
    assertEquals(3, list.size, "5e8485c0-563a-4a73-8941-cfa7d26a43dc")
    assertEquals("fuck", list[0], "77e5fed7-f3d0-403d-b0e7-c426f9fe2dca")
    assertEquals("shit", list[1], "766cd8d1-5d6e-40c2-9721-2351dc6bd1f4")
    assertEquals("bitch", list[2], "8a8e933f-af68-42f5-af92-53e04f661916")
}

external fun explode(delim: String, s: String): Array<String>

@PHPDumpBodyToContainer
fun quickTest_explode() {
    assertVarExportEquals("array ( 0 => 'fuck', 1 => 'shit', 2 => 'bitch',)", explode(":", "fuck:shit:bitch"), "a9c148fb-c3d6-4491-b3ff-aad128e90e4a")
}

fun <T> Array<out T>.toList(): List<T> {
    return this.toMutableList()
}

fun <T> Array<out T>.toMutableList(): MutableList<T> {
    val res = LameList<T>()
    for (x in this) {
        res += x
    }
    return res
}

external fun <T> array_push(arr: Array<T>, x: T)

class LameList<T> : MutableList<T> {
    private val items = arrayOf<T>()

    override val size: Int
        get() = count(items)

    override fun contains(element: T): Boolean {
        imf("aae8d310-7b52-4940-8536-8b296d321d85")
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        imf("02d1e23b-7628-4c11-bd19-6ab0038fc6f5")
    }

    override fun get(index: Int): T {
        return items[index]
    }

    override fun indexOf(element: T): Int {
        imf("33f93e1d-2c16-4fe4-9cf9-ce0a746b438e")
    }

    override fun isEmpty(): Boolean {
        imf("9ed7ece7-2716-4185-b7ae-5bc8ee6feb65")
    }

    override fun iterator(): MutableIterator<T> {
        imf("a1b4c53d-af37-4e01-a018-e2efd0fdb528")
    }

    override fun lastIndexOf(element: T): Int {
        imf("dd1ffe9d-7e43-48e6-bcce-078aad288fbe")
    }

    override fun add(element: T): Boolean {
        "rrrrrrrrrrrrrrrrrrrrrrr"
        array_push(items, element)
        return true
    }

    override fun add(index: Int, element: T) {
        imf("7793122a-3049-4db0-9fa7-41d512dc602b")
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        imf("66e8ca28-12d2-4b56-8fe5-5884d6ca4d3b")
    }

    override fun addAll(elements: Collection<T>): Boolean {
        imf("857afc62-5994-4184-b764-2a7e07b19220")
    }

    override fun clear() {
        imf("675987d1-4f60-4b2a-ae90-84d6483997db")
    }

    override fun listIterator(): MutableListIterator<T> {
        imf("b39fdde2-12fc-4f17-95f1-b0f00f64c099")
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        imf("c06dad13-215a-450f-a003-d70119c39742")
    }

    override fun remove(element: T): Boolean {
        imf("2c9b67e9-a515-4eac-b299-96edbe94c174")
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        imf("c3c3fef4-d8bb-4e4c-8a52-991bbb02978b")
    }

    override fun removeAt(index: Int): T {
        imf("c167d49d-9d1a-4cdc-a678-0ee6c35867d6")
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        imf("d86c44b1-e46d-4886-89e5-e36a7af434c9")
    }

    override fun set(index: Int, element: T): T {
        imf("3f457dce-d477-4147-82d8-ead4a4b1ccf5")
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        imf("a2cf6445-99c9-4c5f-8487-bdd4a193d931")
    }

}

























