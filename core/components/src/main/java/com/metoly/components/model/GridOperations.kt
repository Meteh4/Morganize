package com.metoly.components.model

import com.metoly.morganize.core.model.grid.GridItem
import com.metoly.morganize.core.model.grid.NotePage
import java.util.UUID

/** Checks AABB overlap between [candidate] and every other item on [page]. */
fun isOverlapping(candidate: GridItem, page: NotePage): Boolean =
    page.items.any { existing ->
        existing.id != candidate.id &&
                candidate.x < existing.x + existing.width &&
                candidate.x + candidate.width > existing.x &&
                candidate.y < existing.y + existing.height &&
                candidate.y + candidate.height > existing.y
    }

/** Updates a single grid item inside a specific page, with overlap protection. */
fun List<NotePage>.updateItem(
    pageId: String,
    itemId: String,
    revertOnOverlap: Boolean = false,
    transform: (GridItem) -> GridItem
): List<NotePage> = map { page ->
    if (page.id != pageId) return@map page

    var reverted = false
    val newItems = page.items.map { item ->
        if (item.id != itemId) return@map item
        val candidate = transform(item)
        if (revertOnOverlap && isOverlapping(candidate, page)) {
            reverted = true
            item
        } else candidate
    }
    if (reverted) page else page.copy(items = newItems)
}

/** Updates a grid item without overlap checking (e.g. text/span changes). */
fun List<NotePage>.updateItemSimple(
    pageId: String,
    itemId: String,
    transform: (GridItem) -> GridItem
): List<NotePage> = map { page ->
    if (page.id != pageId) return@map page
    page.copy(items = page.items.map { if (it.id == itemId) transform(it) else it })
}

/** Removes an item from a specific page. */
fun List<NotePage>.removeItem(pageId: String, itemId: String): List<NotePage> =
    map { page ->
        if (page.id != pageId) page
        else page.copy(items = page.items.filter { it.id != itemId })
    }

/** Adds a grid item to the last page, creating one if none exist. */
fun List<NotePage>.addItemToLastPage(item: GridItem): List<NotePage> {
    val mutable = toMutableList()
    if (mutable.isEmpty()) {
        mutable.add(NotePage(id = UUID.randomUUID().toString(), items = listOf(item)))
    } else {
        val last = mutable.last()
        mutable[mutable.lastIndex] = last.copy(items = last.items + item)
    }
    return mutable
}