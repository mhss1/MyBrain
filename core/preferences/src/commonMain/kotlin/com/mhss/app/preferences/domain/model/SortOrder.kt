package com.mhss.app.preferences.domain.model

sealed class SortType {
    data object ASC : SortType()
    data object DESC : SortType()
}
sealed class SortOrder(val sortType: SortType) {
    abstract fun copyOrder(sortType: SortType): SortOrder

    data class Alphabetical(val type: SortType = SortType.ASC) : SortOrder(type) {
        override fun copyOrder(sortType: SortType): SortOrder {
            return this.copy(type = sortType)
        }
    }

    data class DateCreated(val type: SortType = SortType.DESC) : SortOrder(type) {
        override fun copyOrder(sortType: SortType): SortOrder {
            return this.copy(type = sortType)
        }
    }

    data class DateModified(val type: SortType = SortType.DESC) : SortOrder(type) {
        override fun copyOrder(sortType: SortType): SortOrder {
            return this.copy(type = sortType)
        }
    }

    data class Priority(val type: SortType = SortType.ASC) : SortOrder(type) {
        override fun copyOrder(sortType: SortType): SortOrder {
            return this.copy(type = sortType)
        }
    }

    data class DueDate(val type: SortType = SortType.ASC) : SortOrder(type) {
        override fun copyOrder(sortType: SortType): SortOrder {
            return this.copy(type = sortType)
        }
    }

    data class Done(val type: SortType = SortType.ASC) : SortOrder(type) {
        override fun copyOrder(sortType: SortType): SortOrder {
            return this.copy(type = sortType)
        }
    }
}

fun Int.toSortOrder(): SortOrder {
    return when(this){
        0 -> SortOrder.Alphabetical(SortType.ASC)
        1 -> SortOrder.DateCreated(SortType.ASC)
        2 -> SortOrder.DateModified(SortType.ASC)
        3 -> SortOrder.Priority(SortType.ASC)
        8 -> SortOrder.DueDate(SortType.ASC)
        10 -> SortOrder.Done(SortType.ASC)
        4 -> SortOrder.Alphabetical(SortType.DESC)
        5 -> SortOrder.DateCreated(SortType.DESC)
        6 -> SortOrder.DateModified(SortType.DESC)
        7 -> SortOrder.Priority(SortType.DESC)
        9 -> SortOrder.DueDate(SortType.DESC)
        11 -> SortOrder.Done(SortType.DESC)
        else -> SortOrder.Alphabetical(SortType.ASC)
    }
}
fun SortOrder.toInt(): Int {
    return when (this.sortType) {
        is SortType.ASC -> {
            when (this) {
                is SortOrder.Alphabetical -> 0
                is SortOrder.DateCreated -> 1
                is SortOrder.DateModified -> 2
                is SortOrder.Priority -> 3
                is SortOrder.DueDate -> 8
                is SortOrder.Done -> 10
            }
        }
        is SortType.DESC -> {
            when (this) {
                is SortOrder.Alphabetical -> 4
                is SortOrder.DateCreated -> 5
                is SortOrder.DateModified -> 6
                is SortOrder.Priority -> 7
                is SortOrder.DueDate -> 9
                is SortOrder.Done -> 11
            }
        }
    }
}