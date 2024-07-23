package com.voxplanapp.model

sealed class ActionMode {
    object Normal : ActionMode()
    object VerticalUp : ActionMode()
    object VerticalDown : ActionMode()
    object HierarchyUp : ActionMode()
    object HierarchyDown : ActionMode()
}
