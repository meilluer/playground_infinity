package ml.docilealligator.infinityforreddit.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.comment.CommentDraft
import ml.docilealligator.infinityforreddit.comment.DraftType
import ml.docilealligator.infinityforreddit.repositories.CommentActivityRepository

class CommentActivityViewModel(
    private val commentActivityRepository: CommentActivityRepository
): ViewModel() {
    fun getCommentDraft(fullname: String): LiveData<CommentDraft> {
        return commentActivityRepository.getCommentDraft(fullname)
    }

    fun saveCommentDraft(fullname: String, content: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            commentActivityRepository.saveCommentDraft(fullname, content)
            onSaved()
        }
    }

    fun deleteCommentDraft(fullname: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            commentActivityRepository.deleteCommentDraft(fullname)
            onDeleted()
        }
    }

    companion object {
        fun provideFactory(commentActivityRepository: CommentActivityRepository) : ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return CommentActivityViewModel(
                        commentActivityRepository,
                    ) as T
                }
            }
        }
    }
}