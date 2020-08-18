package com.hannesdorfmann.sealedclass.generator

import org.junit.Assert
import  org.junit.Test
import java.lang.IllegalArgumentException

class SealedClassGeneratorTest {

    @Test
    fun `it creates instances`() {
        val instances: List<SealedClassRoot> = instantiateSealedSubclasses(
            SealedClassRoot::class,
            primitiveTypeValueGenerator = IncrementalPrimitiveTypeGenerator()
        )
        val expected: List<SealedClassRoot> = listOf(
            SealedClassRoot.SingletonObject,
            SealedClassRoot.EmptyClass(),
            SealedClassRoot.Primitives(
                i = 1,
                oI = null,
                dI = 2,
                b = true,
                oB = null,
                dB = true,
                d = 1.0,
                oD = null,
                dD = 123.2,
                f = 1.0f,
                oF = null,
                dF = 12.44f,
                l = 1,
                oL = null,
                dL = 12356,
                s = 1,
                oS = null,
                dS = 123,
                by = 1,
                oBy = null,
                dBy = 12,
                c = 'a',
                oC = null,
                dC = 'h',
                str = "str1",
                oStr = null,
                dStr = "default Str"
            ),
            SealedClassRoot.OtherDataClass(Other(i = 2)),
            SealedClassRoot.OtherNotDataClass(Other(i = 3)),
            SealedClassRoot.Enum(ExampleEnum.B)
        )

        Assert.assertEquals(expected, instances)
    }

    @Test
    fun `throw error if not sealed class`() {
        try {
            instantiateSealedSubclasses(Unit::class)
        } catch (e: IllegalArgumentException) {
            val expectedMsg = "Only a Class Type that is a sealead class are allowed"
            Assert.assertEquals(expectedMsg, e.message)
        }
    }

}

sealed class SealedClassRoot {
    object SingletonObject : SealedClassRoot()
    class EmptyClass() : SealedClassRoot() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    data class Primitives(
        val i: Int, val oI: Int?, val dI: Int = 2,
        val b: Boolean, val oB: Boolean?, val dB: Boolean = true,
        val d: Double, val oD: Double?, val dD: Double = 123.2,
        val f: Float, val oF: Float?, val dF: Float = 12.44f,
        val l: Long, val oL: Long?, val dL: Long = 12356,
        val s: Short, val oS: Short?, val dS: Short = 123,
        val by: Byte, val oBy: Byte?, val dBy: Byte = 12,
        val c: Char, val oC: Char?, val dC: Char = 'h',
        val str: String, val oStr: String?, val dStr: String = "default Str"
    ) : SealedClassRoot()

    data class OtherDataClass(val other: Other) : SealedClassRoot()

    class OtherNotDataClass(val other: Other) : SealedClassRoot() {
        override fun toString(): String {
            return super.toString() + ("other=$other")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OtherNotDataClass

            if (this.other != other.other) return false

            return true
        }

        override fun hashCode(): Int {
            return other.hashCode()
        }

    }

    data class Enum(val enum: ExampleEnum) : SealedClassRoot()
    // class GenericAction(val t: List<String>) : PaywallAction()
}

data class Other(val i: Int)

enum class ExampleEnum {
    A,
    B
}