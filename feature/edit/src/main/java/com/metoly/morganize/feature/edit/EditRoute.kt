package com.metoly.morganize.feature.edit

import kotlinx.serialization.Serializable

/**
 * Navigation3 route for the Edit screen. [noteId] is passed as part of the route so that
 * Navigation3 can safely reconstruct the destination from the back stack.
 */
@Serializable data class EditRoute(val noteId: Long)
