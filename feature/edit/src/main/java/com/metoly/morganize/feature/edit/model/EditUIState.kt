package com.metoly.morganize.feature.edit.model

import com.metoly.morganize.core.model.ResponseState

data class EditSpecificState(
    val noteState: ResponseState<Unit> = ResponseState.Idle,
    val showDeleteDialog: Boolean = false
)