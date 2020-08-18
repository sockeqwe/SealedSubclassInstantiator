package com.hannesdorfmann.sealedclass.generator

import kotlin.experimental.and
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

fun <T : Any> instantiateSealedSubclasses(clazz: KClass<T>, primitiveTypeValueGenerator: PrimitiveTypeValueGenerator = RandomPrimitiveTypeValueGenerator()): List<T> {
    val sealedSubClasses = clazz.sealedSubclasses
    if (sealedSubClasses.isEmpty())
        throw IllegalArgumentException("Only a Class Type that is a sealead class are allowed")

    return sealedSubClasses.map { subClass ->
        instantiateObject(subClass, primitiveTypeValueGenerator)
    }
}

private fun <T : Any> instantiateObject(clazz: KClass<T>, primitiveTypeValueGenerator: PrimitiveTypeValueGenerator): T {
    val objectInstance = clazz.objectInstance
    if (objectInstance != null) // It's a singleton object
        return objectInstance

    if (clazz.typeParameters.isNotEmpty()) {
        // TODO Can generics be supported?
        throw UnsupportedOperationException("Generics are not supported but $clazz uses generics")
    }

    val primaryConstructor = clazz.primaryConstructor ?: throw UnsupportedOperationException(
            "Can not instantiate an instance of ${clazz} without a primary constructor"
    )

    val primaryConstructorParameters = primaryConstructor.parameters.associate { parameter ->
        if (parameter.kind != KParameter.Kind.VALUE) {
            throw UnsupportedOperationException("Only ordinary constructor parameter values " +
                    "are allowed but primary constructor of $clazz needs an unsupporter parameter $parameter")
        }

        val type = parameter.type
        parameter to
                when {
                    parameter.isVararg -> {
                        // TODO support varargs
                        null
                    }
                    parameter.isOptional -> {
                        // Short cut. Return null. Afterwards there is a .filter{ }
                        // that filters out optional parameters to ensure that default value is used instead of null
                        null
                    }
                    type.isMarkedNullable -> {
                        // TODO could sometimes return a not nullable
                        null
                    }
                    type.classifier == null -> {
                        throw UnsupportedOperationException("Only traditional type are supported but $parameter " +
                                "is not traditional in primary constructor for $clazz")
                    }
                    else -> {
                        // TODO add support for interfaces

                        when (val parameterTypeAsString = type.toString()) {
                            "kotlin.Int" -> primitiveTypeValueGenerator.nextInt()
                            "kotlin.Boolean" -> primitiveTypeValueGenerator.nextBoolean()
                            "kotlin.Double" -> primitiveTypeValueGenerator.nextDouble()
                            "kotlin.Float" -> primitiveTypeValueGenerator.nextFloat()
                            "kotlin.Long" -> primitiveTypeValueGenerator.nextLong()
                            "kotlin.Short" -> primitiveTypeValueGenerator.nextShort()
                            "kotlin.Byte" -> primitiveTypeValueGenerator.nextByte()
                            "kotlin.Char" -> primitiveTypeValueGenerator.nextChar()
                            "kotlin.String" -> primitiveTypeValueGenerator.nextString()
                            else -> {
                                /*
                                if (parameterTypeAsString.startsWith("kotlin.collections.List")) {
                                    if (type.arguments.size == 1) {
                                        val clazzOfList = type.arguments[0].type!!
                                        listOf(instantiateObject(clazzOfList.classifier., primitiveTypeValueGenerator))
                                    } else {
                                        throw UnsupportedOperationException()
                                    }

                                 */

                                /*
                                if (parameterTypeAsString.startsWith("kotlin.collections.List")) {
                                    if (type.arguments.size != 1) {
                                        throw IllegalStateException("A List must have exactly one generic " +
                                                "type but that is not the case for $parameter in constructor of $clazz. " +
                                                "How could this happen?")
                                    }

                                    listOf(instantiateObject(clazz = type.arguments[0].type!!,
                                            primitiveTypeValueGenerator = primitiveTypeValueGenerator))

                                    throw UnsupportedOperationException()
                                }

                                 */

                                val parameterClass = Class.forName(parameterTypeAsString)
                                if (parameterClass.isEnum) {
                                    parameterClass.enumConstants[
                                            primitiveTypeValueGenerator.nextIndexForEnumConstants(
                                                    parameterClass.enumConstants.size
                                            )
                                    ]
                                } else {
                                    instantiateObject(parameterClass.kotlin, primitiveTypeValueGenerator)
                                }

                            }

                        }
                    }
                }
    }.filter { entry -> !entry.key.isOptional } // optionals

    return if (primaryConstructorParameters.isEmpty()) {
        primaryConstructor.call()
    } else {
        primaryConstructor.callBy(primaryConstructorParameters)
    }
}

interface PrimitiveTypeValueGenerator {
    fun nextInt(): Int
    fun nextBoolean(): Boolean
    fun nextDouble(): Double
    fun nextFloat(): Float
    fun nextLong(): Long
    fun nextShort(): Short
    fun nextByte(): Byte
    fun nextChar(): Char
    fun nextString(): String

    /**
     * If we have an enum type, we need an index to choose one of the Enum Constants.
     * This method returns the index for that enum.
     */
    fun nextIndexForEnumConstants(max: Int): Int
}

private class RandomPrimitiveTypeValueGenerator : PrimitiveTypeValueGenerator {
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    override fun nextIndexForEnumConstants(enumConstantsSize: Int): Int = Random.nextInt(enumConstantsSize)
    override fun nextInt(): Int = Random.nextInt()
    override fun nextBoolean(): Boolean = Random.nextBoolean()
    override fun nextDouble(): Double = Random.nextDouble()
    override fun nextFloat(): Float = Random.nextFloat()
    override fun nextLong(): Long = Random.nextLong()
    override fun nextShort(): Short = Short.MAX_VALUE // TODO make this random?
    override fun nextByte(): Byte = Random.nextBytes(1)[0] // TODO optimize this
    override fun nextChar(): Char = charPool[Random.nextInt(charPool.size)]
    override fun nextString(): String {
        val bytes = Random.nextBytes(10)
        return (bytes.indices)
                .map { i ->
                    charPool[(bytes[i] and 0xFF.toByte() and (charPool.size - 1).toByte()).toInt()]
                }.joinToString("")
    }
}
