package chat.rocket.android.helper

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

abstract class LoadMoreScrollListener(private val layoutManager: LinearLayoutManager, private val visibleThreshold: Int) : RecyclerView.OnScrollListener() {
    private var isLoading: Boolean = false

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
    }
    override fun onScrolled(recyclerView: RecyclerView, deltaX: Int, deltaY: Int) {
        super.onScrolled(recyclerView, deltaX, deltaY)

        val visibleItemCount = recyclerView.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        val findLastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

        if (!isLoading
                && findLastVisibleItemPosition + visibleThreshold>= totalItemCount-1
                && visibleItemCount < totalItemCount
                && deltaY < -10) {
            isLoading = true
            requestMoreItem()
        }
    }

    fun setLoadingDone() {
        isLoading = false
    }

    abstract fun requestMoreItem()
}