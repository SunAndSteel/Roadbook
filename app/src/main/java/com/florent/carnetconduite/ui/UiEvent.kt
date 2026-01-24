package com.florent.carnetconduite.ui

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowConfirmDialog(
        val title: String,
        val message: String,
        val onConfirm: () -> Unit
    ) : UiEvent()
    data class ShowError(val message: String) : UiEvent()
}