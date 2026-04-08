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

/** Adds a grid item to the nearest available space on the last page. If no space exists, creates a new page. */
fun List<NotePage>.addItemToLastPage(
    item: GridItem,
    columns: Int = 10,
    rows: Int = 20
): List<NotePage> {
    val mutable = toMutableList()

    fun findSpace(page: NotePage): Pair<Int, Int>? {
        if (item.width > columns || item.height > rows) return null
        for (y in 0 .. rows - item.height) {
            for (x in 0 .. columns - item.width) {
                val hasOverlap = page.items.any { existing ->
                    x < existing.x + existing.width &&
                    x + item.width > existing.x &&
                    y < existing.y + existing.height &&
                    y + item.height > existing.y
                }
                if (!hasOverlap) return x to y
            }
        }
        return null
    }

    fun positionItem(x: Int, y: Int): GridItem {
        return when (item) {
            is GridItem.Checklist -> item.copy(x = x, y = y)
            is GridItem.Image -> item.copy(x = x, y = y)
            is GridItem.Text -> item.copy(x = x, y = y)
        }
    }

    if (mutable.isEmpty()) {
        mutable.add(NotePage(id = UUID.randomUUID().toString(), items = listOf(positionItem(0, 0))))
        return mutable
    }

    val last = mutable.last()
    val space = findSpace(last)

    if (space != null) {
        val (x, y) = space
        mutable[mutable.lastIndex] = last.copy(items = last.items + positionItem(x, y))
    } else {
        mutable.add(NotePage(id = UUID.randomUUID().toString(), items = listOf(positionItem(0, 0))))
    }

    return mutable
}