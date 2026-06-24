package com.meilluer.infinity.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import com.meilluer.infinity.comment.CommentDraft
import com.meilluer.infinity.comment.DraftType
import com.meilluer.infinity.repositories.EditCommentActivityRepository

class EditCommentActivityViewModel(
    private val editCommentActivityRepository: EditCommentActivityRepository
): ViewModel() {
    fun getCommentDraft(fullname: String): LiveData<CommentDraft> {
        return editCommentActivityRepository.getCommentDraft(fullname)
    }

    fun saveCommentDraft(fullname: String, content: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            editCommentActivityRepository.saveCommentDraft(fullname, content)
            onSaved()
        }
    }

    fun deleteCommentDraft(fullname: String,  onDeleted: () -> Unit) {
        viewModelScope.launch {
            editCommentActivityRepository.deleteCommentDraft(fullname)
            onDeleted()
        }
    }

    companion object {
        fun provideFactory(editCommentActivityRepository: EditCommentActivityRepository) : ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return EditCommentActivityViewModel(
                        editCommentActivityRepository,
                    ) as T
                }
            }
        }
    }
}