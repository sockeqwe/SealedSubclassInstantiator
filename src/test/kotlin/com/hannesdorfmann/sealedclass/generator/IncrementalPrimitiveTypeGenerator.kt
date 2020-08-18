package com.hannesdorfmann.sealedclass.generator

class IncrementalPrimitiveTypeGenerator : PrimitiveTypeValueGenerator {
    private var nextInt: Int = 1
    private var nextBoolean: Int = 1
    private var nextDouble: Double = 1.0
    private var nextFloat: Float = 1.0f
    private var nextLong: Long = 1
    private var nextShort: Short = 1
    private var nextByte: Short = 1
    private var nextChar: Char = 'a'
    private var nextStringCounter: Long = 1
    private var nextEnumIndexCounter: Int = 1

    override fun nextInt(): Int = nextInt++

    override fun nextBoolean(): Boolean = (nextBoolean++) % 2 == 1

    override fun nextDouble(): Double = nextDouble++

    override fun nextFloat(): Float = nextFloat++

    override fun nextLong(): Long = nextLong++

    override fun nextShort(): Short = nextShort++

    override fun nextByte(): Byte = nextByte++.toByte()

    override fun nextChar(): Char = nextChar++

    override fun nextString(): String = "str$nextStringCounter"

    override fun nextIndexForEnumConstants(max: Int): Int = (nextEnumIndexCounter++) % 2
}