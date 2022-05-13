package com.mhss.app.mybrain.util.settings


enum class ThemeSettings(val value: Int) {
    LIGHT(0),
    DARK(1),
    AUTO(2)
}

sealed class OrderType {
    object ASC : OrderType()
    object DESC : OrderType()
}
sealed class Order(val type: OrderType){
    class Alphabetical(type: OrderType) : Order(type)
    class DateCreated(type: OrderType) : Order(type)
    class DateModified(type: OrderType) : Order(type)
    class Custom<Class, VarType : Comparable<VarType>> constructor(val selector: (Class) -> VarType?, type: OrderType) : Order(type)
}